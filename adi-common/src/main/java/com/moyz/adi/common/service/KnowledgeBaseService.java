package com.moyz.adi.common.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.ChainWrappers;
import com.moyz.adi.common.base.ThreadContext;
import com.moyz.adi.common.cosntant.AdiConstant;
import com.moyz.adi.common.cosntant.RedisKeyConstant;
import com.moyz.adi.common.dto.KbEditReq;
import com.moyz.adi.common.dto.KbInfoResp;
import com.moyz.adi.common.dto.KbSearchReq;
import com.moyz.adi.common.entity.*;
import com.moyz.adi.common.exception.BaseException;
import com.moyz.adi.common.file.FileOperatorContext;
import com.moyz.adi.common.helper.LLMContext;
import com.moyz.adi.common.helper.SSEEmitterHelper;
import com.moyz.adi.common.mapper.KnowledgeBaseMapper;
import com.moyz.adi.common.rag.*;
import com.moyz.adi.common.service.embedding.IEmbeddingService;
import com.moyz.adi.common.util.*;
import com.moyz.adi.common.vo.*;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.store.embedding.filter.comparison.IsEqualTo;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.text.MessageFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.moyz.adi.common.cosntant.AdiConstant.RetrieveContentFrom.KNOWLEDGE_BASE;
import static com.moyz.adi.common.cosntant.AdiConstant.SSE_TIMEOUT;
import static com.moyz.adi.common.cosntant.AdiConstant.SysConfigKey.QUOTA_BY_QA_ASK_DAILY;
import static com.moyz.adi.common.cosntant.RedisKeyConstant.KB_STATISTIC_RECALCULATE_SIGNAL;
import static com.moyz.adi.common.cosntant.RedisKeyConstant.USER_INDEXING;
import static com.moyz.adi.common.enums.ErrorEnum.*;
import static com.moyz.adi.common.util.LocalDateTimeUtil.PATTERN_YYYY_MM_DD;

/**
 * 知识库管理服务。
 */
@Slf4j
@Service
public class KnowledgeBaseService extends ServiceImpl<KnowledgeBaseMapper, KnowledgeBase> {

    /**
     * 自身代理对象（用于触发异步方法）。
     */
    @Lazy
    @Resource
    private KnowledgeBaseService self;

    /**
     * Redis 操作模板。
     */
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 知识点服务。
     */
    @Resource
    private KnowledgeBaseItemService knowledgeBaseItemService;

    /**
     * 问答记录服务。
     */
    @Resource
    private KnowledgeBaseQaService knowledgeBaseQaRecordService;

    /**
     * 知识库收藏服务。
     */
    @Resource
    private KnowledgeBaseStarService knowledgeBaseStarRecordService;

    /**
     * 文件服务。
     */
    @Resource
    private FileService fileService;

    /**
     * SSE 发送辅助。
     */
    @Resource
    private SSEEmitterHelper sseEmitterHelper;

    /**
     * 用户日消耗统计服务。
     */
    @Resource
    private UserDayCostService userDayCostService;

    /**
     * 模型服务。
     */
    @Resource
    private AiModelService aiModelService;

    /**
     * 向量服务。
     */
    @Resource
    private IEmbeddingService embeddingService;

    /**
     * 新增或更新知识库。
     *
     * @param kbEditReq 编辑请求
     * @return 知识库实体
     */
    public KnowledgeBase saveOrUpdate(KbEditReq kbEditReq) {
        KnowledgeBase knowledgeBase = new KnowledgeBase();
        BeanUtils.copyProperties(kbEditReq, knowledgeBase, "id", "uuid", "ingestTokenizer", "ingestEmbeddingModel");
        if (null != kbEditReq.getIngestModelId() && kbEditReq.getIngestModelId() > 0) {
            knowledgeBase.setIngestModelName(aiModelService.getByIdOrThrow(kbEditReq.getIngestModelId()).getName());
        } else {
            //没有指定抽取图谱知识时的LLM时，自动指定第一个可用的
            LLMContext.getFirstEnableAndFree().ifPresent(llmService -> {
                knowledgeBase.setIngestModelName(llmService.getAiModel().getName());
                knowledgeBase.setIngestModelId(llmService.getAiModel().getId());
            });
        }
        if (StringUtils.isNotBlank(kbEditReq.getIngestTokenEstimator()) && AdiConstant.TokenEstimator.ALL.contains(kbEditReq.getIngestTokenEstimator())) {
            knowledgeBase.setIngestTokenEstimator(kbEditReq.getIngestTokenEstimator());
        }
        if (null == kbEditReq.getId() || kbEditReq.getId() < 1) {
            User user = ThreadContext.getCurrentUser();
            knowledgeBase.setUuid(UuidUtil.createShort());
            knowledgeBase.setOwnerId(user.getId());
            knowledgeBase.setOwnerUuid(user.getUuid());
            knowledgeBase.setOwnerName(user.getName());
            baseMapper.insert(knowledgeBase);
        } else {
            checkPrivilege(kbEditReq.getId(), null);
            knowledgeBase.setId(kbEditReq.getId());
            baseMapper.updateById(knowledgeBase);
        }
        return knowledgeBase;
    }

    /**
     * 批量上传文档并可选择索引。
     *
     * @param kbUuid     知识库 UUID
     * @param embedding  是否进行向量索引
     * @param docs       文档数组
     * @param indexTypes 索引类型
     * @return 文件记录列表
     */
    public List<AdiFile> uploadDocs(String kbUuid, Boolean embedding, MultipartFile[] docs, List<String> indexTypes) {
        if (ArrayUtils.isEmpty(docs)) {
            return Collections.emptyList();
        }
        checkPrivilege(null, kbUuid);
        List<AdiFile> result = new ArrayList<>();
        KnowledgeBase knowledgeBase = ChainWrappers.lambdaQueryChain(baseMapper)
                .eq(KnowledgeBase::getUuid, kbUuid)
                .eq(KnowledgeBase::getIsDeleted, false)
                .oneOpt()
                .orElseThrow(() -> new BaseException(A_DATA_NOT_FOUND));
        for (MultipartFile doc : docs) {
            try {
                result.add(uploadDoc(knowledgeBase, doc, embedding, indexTypes));
            } catch (Exception e) {
                log.warn("uploadDocs fail,fileName:{}", doc.getOriginalFilename(), e);
            }
        }
        return result;
    }

    /**
     * 上传单个文档并可选择索引。
     *
     * @param kbUuid           知识库 UUID
     * @param indexAfterUpload 是否上传后索引
     * @param doc              文档
     * @param indexTypes       索引类型
     * @return 文件记录
     */
    public AdiFile uploadDoc(String kbUuid, Boolean indexAfterUpload, MultipartFile doc, List<String> indexTypes) {
        KnowledgeBase knowledgeBase = ChainWrappers.lambdaQueryChain(baseMapper)
                .eq(KnowledgeBase::getUuid, kbUuid)
                .eq(KnowledgeBase::getIsDeleted, false)
                .oneOpt()
                .orElseThrow(() -> new BaseException(A_DATA_NOT_FOUND));
        return uploadDoc(knowledgeBase, doc, indexAfterUpload, indexTypes);
    }

    /**
     * 上传文档并生成知识点记录。
     *
     * @param knowledgeBase    知识库
     * @param doc              文档
     * @param indexAfterUpload 是否上传后索引
     * @param indexTypes       索引类型
     * @return 文件记录
     */
    private AdiFile uploadDoc(KnowledgeBase knowledgeBase, MultipartFile doc, Boolean indexAfterUpload, List<String> indexTypes) {
        try {
            String fileName = doc.getOriginalFilename();
            AdiFile adiFile = fileService.saveFile(doc, false);

            //解析文档
            Document document = FileOperatorContext.loadDocument(adiFile);
            if (null == document) {
                log.warn("该文件类型:{}无法解析，忽略", adiFile.getExt());
                return adiFile;
            }
            //创建知识库条目
            String uuid = UuidUtil.createShort();
            // PostgreSQL 不支持 \u0000
            String content = document.text().replace("\u0000", "");
            KnowledgeBaseItem knowledgeBaseItem = new KnowledgeBaseItem();
            knowledgeBaseItem.setUuid(uuid);
            knowledgeBaseItem.setKbId(knowledgeBase.getId());
            knowledgeBaseItem.setKbUuid(knowledgeBase.getUuid());
            knowledgeBaseItem.setSourceFileId(adiFile.getId());
            knowledgeBaseItem.setTitle(fileName);
            knowledgeBaseItem.setBrief(StringUtils.substring(content, 0, 200));
            knowledgeBaseItem.setRemark(content);
            boolean success = knowledgeBaseItemService.save(knowledgeBaseItem);
            if (success && Boolean.TRUE.equals(indexAfterUpload)) {
                indexItems(List.of(uuid), indexTypes);
            }

            // 将文件路径替换为访问 URL
            adiFile.setPath(FileOperatorContext.getFileUrl(adiFile));
            return adiFile;
        } catch (Exception e) {
            log.error("upload error", e);
            throw new BaseException(A_UPLOAD_FAIL);
        }
    }

    /**
     * 索引（向量化、图谱化）
     *
     * @param kbUuid     知识库uuid
     * @param indexTypes 索引类型，如embedding,graphical
     * @return 成功或失败
     */
    public boolean indexing(String kbUuid, List<String> indexTypes) {
        checkPrivilege(null, kbUuid);
        KnowledgeBase knowledgeBase = this.getOrThrow(kbUuid);
        LambdaQueryWrapper<KnowledgeBaseItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KnowledgeBaseItem::getIsDeleted, false);
        wrapper.eq(KnowledgeBaseItem::getUuid, kbUuid);
        BizPager.oneByOneWithAnchor(wrapper, knowledgeBaseItemService, KnowledgeBaseItem::getId, kbItem -> knowledgeBaseItemService.asyncIndex(ThreadContext.getCurrentUser(), knowledgeBase, kbItem, indexTypes));
        return true;
    }

    /**
     * 索引知识点（同一知识库下）
     *
     * @param itemUuids  知识点uuid列表
     * @param indexTypes 索引类型，如embedding,graphical
     * @return 成功或失败
     */
    public boolean indexItems(List<String> itemUuids, List<String> indexTypes) {
        try {
            if (CollectionUtils.isEmpty(itemUuids)) {
                return false;
            }
            KnowledgeBase knowledgeBase = baseMapper.getByItemUuid(itemUuids.get(0));
            String userIndexKey = MessageFormat.format(USER_INDEXING, knowledgeBase.getOwnerId());
            Boolean exist = stringRedisTemplate.hasKey(userIndexKey);
            if (Boolean.TRUE.equals(exist)) {
                log.warn("文档正在索引中,请忽频繁操作,userId:{}", knowledgeBase.getOwnerId());
                throw new BaseException(A_DOC_INDEX_DOING);
            }
            return knowledgeBaseItemService.checkAndIndexing(knowledgeBase, itemUuids, indexTypes);
        } catch (BaseException e) {
            log.error("indexAfterUpload error", e);
        }
        return false;
    }

    /**
     * 检查当前用户下的索引任务是否已经结束
     *
     * @return 成功或失败
     */
    public boolean checkIndexIsFinish() {
        String userIndexKey = MessageFormat.format(USER_INDEXING, ThreadContext.getCurrentUserId());
        return Boolean.FALSE.equals(stringRedisTemplate.hasKey(userIndexKey));
    }

    /**
     * 分页查询当前用户的知识库。
     *
     * @param keyword             关键词
     * @param includeOthersPublic 是否包含他人公开知识库
     * @param currentPage         当前页
     * @param pageSize            页大小
     * @return 分页结果
     */
    public Page<KbInfoResp> searchMine(String keyword, Boolean includeOthersPublic, Integer currentPage, Integer pageSize) {
        Page<KbInfoResp> result = new Page<>();
        User user = ThreadContext.getCurrentUser();
        Page<KnowledgeBase> knowledgeBasePage;
        if (Boolean.TRUE.equals(user.getIsAdmin())) {
            knowledgeBasePage = baseMapper.searchByAdmin(new Page<>(currentPage, pageSize), keyword);
        } else {
            knowledgeBasePage = baseMapper.searchByUser(new Page<>(currentPage, pageSize), user.getId(), keyword, includeOthersPublic);
        }
        return MPPageUtil.convertToPage(knowledgeBasePage, result, KbInfoResp.class, null);
    }

    /**
     * 分页搜索知识库。
     *
     * @param req         查询条件
     * @param currentPage 当前页
     * @param pageSize    页大小
     * @return 分页结果
     */
    public Page<KbInfoResp> search(KbSearchReq req, Integer currentPage, Integer pageSize) {
        Page<KbInfoResp> result = new Page<>();
        LambdaQueryWrapper<KnowledgeBase> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(req.getTitle())) {
            wrapper.like(KnowledgeBase::getTitle, req.getTitle());
        }
        if (StringUtils.isNotBlank(req.getOwnerName())) {
            wrapper.like(KnowledgeBase::getOwnerName, req.getOwnerName());
        }
        if (null != req.getIsPublic()) {
            wrapper.eq(KnowledgeBase::getIsPublic, req.getIsPublic());
        }
        if (null != req.getMinItemCount()) {
            wrapper.ge(KnowledgeBase::getItemCount, req.getMinItemCount());
        }
        if (null != req.getMinEmbeddingCount()) {
            wrapper.ge(KnowledgeBase::getEmbeddingCount, req.getMinEmbeddingCount());
        }
        if (null != req.getCreateTime() && req.getCreateTime().length == 2) {
            wrapper.between(KnowledgeBase::getCreateTime, LocalDateTimeUtil.parse(req.getCreateTime()[0]), LocalDateTimeUtil.parse(req.getCreateTime()[1]));
        }
        if (null != req.getUpdateTime() && req.getUpdateTime().length == 2) {
            wrapper.between(KnowledgeBase::getUpdateTime, LocalDateTimeUtil.parse(req.getUpdateTime()[0]), LocalDateTimeUtil.parse(req.getUpdateTime()[1]));
        }
        wrapper.eq(KnowledgeBase::getIsDeleted, false);
        wrapper.orderByDesc(KnowledgeBase::getStarCount, KnowledgeBase::getUpdateTime);
        Page<KnowledgeBase> knowledgeBasePage = baseMapper.selectPage(new Page<>(currentPage, pageSize), wrapper);
        return MPPageUtil.convertToPage(knowledgeBasePage, result, KbInfoResp.class, null);
    }

    /**
     * 根据 ID 列表查询知识库。
     *
     * @param ids 知识库 ID 列表
     * @return 知识库信息列表
     */
    public List<KbInfoResp> listByIds(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyList();
        }
        List<KnowledgeBase> knowledgeBases = baseMapper.selectByIds(ids);
        return MPPageUtil.convertToList(knowledgeBases, KbInfoResp.class);
    }

    /**
     * 软删除知识库。
     *
     * @param uuid 知识库 UUID
     * @return 是否删除成功
     */
    public boolean softDelete(String uuid) {
        checkPrivilege(null, uuid);
        return ChainWrappers.lambdaUpdateChain(baseMapper)
                .eq(KnowledgeBase::getUuid, uuid)
                .set(KnowledgeBase::getIsDeleted, true)
                .update();
    }

    /**
     * 以 SSE 方式发起知识库问答。
     *
     * @param qaRecordUuid 问答记录 UUID
     * @return SSE 连接
     */
    public SseEmitter sseAsk(String qaRecordUuid) {
        // 先进行问答限额校验，避免无效 SSE 连接占用资源
        checkRequestTimesOrThrow();
        // 设置统一超时时间，防止长连接无限挂起
        SseEmitter sseEmitter = new SseEmitter(SSE_TIMEOUT);
        // 从线程上下文获取用户，确保请求身份一致
        User user = ThreadContext.getCurrentUser();
        // 若用户校验或并发限制未通过，直接返回已完成的 emitter
        if (!sseEmitterHelper.checkOrComplete(user, sseEmitter)) {
            return sseEmitter;
        }
        // 先建立 SSE 通道，再异步执行耗时检索与模型调用
        sseEmitterHelper.startSse(user, sseEmitter);
        // 通过 self 触发代理以保证 @Async 生效
        self.retrieveAndPushToLLM(user, sseEmitter, qaRecordUuid);
        return sseEmitter;
    }

    /**
     * 收藏或取消收藏知识库。
     *
     * @param user   用户
     * @param kbUuid 知识库 UUID
     * @return true 表示收藏，false 表示取消收藏
     */
    @Transactional
    public boolean toggleStar(User user, String kbUuid) {

        KnowledgeBase knowledgeBase = self.getOrThrow(kbUuid);
        boolean star;
        KnowledgeBaseStar oldRecord = knowledgeBaseStarRecordService.getRecord(user.getId(), kbUuid);
        if (null == oldRecord) {
            KnowledgeBaseStar starRecord = new KnowledgeBaseStar();
            starRecord.setUserId(user.getId());
            starRecord.setUserUuid(user.getUuid());
            starRecord.setKbId(knowledgeBase.getId());
            starRecord.setKbUuid(kbUuid);
            knowledgeBaseStarRecordService.save(starRecord);

            star = true;
        } else {
            // 已删除表示取消收藏
            knowledgeBaseStarRecordService.lambdaUpdate()
                    .eq(KnowledgeBaseStar::getId, oldRecord.getId())
                    .set(KnowledgeBaseStar::getIsDeleted, !oldRecord.getIsDeleted())
                    .update();
            star = oldRecord.getIsDeleted();
        }
        int starCount = star ? knowledgeBase.getStarCount() + 1 : knowledgeBase.getStarCount() - 1;
        ChainWrappers.lambdaUpdateChain(baseMapper)
                .eq(KnowledgeBase::getId, knowledgeBase.getId())
                .set(KnowledgeBase::getStarCount, starCount)
                .update();
        return star;
    }

    /**
     * 知识库问答限额判断。
     *
     * @return 无
     * @throws BaseException 超出限额时抛出异常
     */
    private void checkRequestTimesOrThrow() {
        // 以“用户 + 日期”为维度计数，保证按天限额统计
        String key = MessageFormat.format(RedisKeyConstant.AQ_ASK_TIMES, ThreadContext.getCurrentUserId(), LocalDateTimeUtil.format(LocalDateTime.now(), PATTERN_YYYY_MM_DD));
        // 获取当前已请求次数与系统配置的每日限额
        String askTimes = stringRedisTemplate.opsForValue().get(key);
        String askQuota = SysConfigService.getByKey(QUOTA_BY_QA_ASK_DAILY);
        // 超过限额直接拒绝，避免后续链路浪费资源
        if (null != askQuota && null != askTimes && Integer.parseInt(askTimes) >= Integer.parseInt(askQuota)) {
            throw new BaseException(A_QA_ASK_LIMIT);
        }
        // 记录本次请求并设置过期时间，避免旧统计长期占用
        stringRedisTemplate.opsForValue().increment(key);
        stringRedisTemplate.expire(key, Duration.ofDays(1));
    }

    /**
     * 文档召回并将请求发送给LLM
     *
     * @param user         当前提问的用户
     * @param sseEmitter   sse emitter
     * @param qaRecordUuid 知识库uuid
     * @return 无
     */
    @Async
    public void retrieveAndPushToLLM(User user, SseEmitter sseEmitter, String qaRecordUuid) {
        // 异步线程记录关键参数，便于追踪与排查问题
        log.info("retrieveAndPushToLLM,qaRecordUuid:{},userId:{}", qaRecordUuid, user.getId());
        // 预先加载问答记录、知识库与模型配置，保证参数一致性
        KnowledgeBaseQa qaRecord = knowledgeBaseQaRecordService.getOrThrow(qaRecordUuid);
        KnowledgeBase knowledgeBase = getOrThrow(qaRecord.getKbUuid());
        AiModel aiModel = aiModelService.getByIdOrThrow(qaRecord.getAiModelId());

        // 使用知识库配置的估算器，确保 token 统计口径一致
        TokenEstimatorThreadLocal.setTokenEstimator(knowledgeBase.getIngestTokenEstimator());

        int maxInputTokens = aiModel.getMaxInputTokens();
        int maxResults = knowledgeBase.getRetrieveMaxResults();
        // 最大召回数量小于 1 时，由系统根据模型最大输入令牌数自动计算
        if (maxResults < 1) {
            maxResults = EmbeddingRag.getRetrieveMaxResults(qaRecord.getQuestion(), maxInputTokens);
        }

        // 组装 SSE 请求参数，确保与问答记录绑定
        SseAskParams sseAskParams = new SseAskParams();
        sseAskParams.setUuid(qaRecord.getUuid());
        sseAskParams.setHttpRequestParams(
                ChatModelRequestParams.builder()
                        // 使用“知识库UUID_用户UUID”隔离不同用户与知识库的记忆上下文
                        .memoryId(qaRecord.getKbUuid() + "_" + user.getUuid())
                        .systemMessage(knowledgeBase.getQuerySystemMessage())
                        .userMessage(qaRecord.getQuestion())
                        .build()
        );
        sseAskParams.setModelProperties(
                ChatModelBuilderProperties.builder()
                        // 使用知识库侧温度配置保持回答风格一致
                        .temperature(knowledgeBase.getQueryLlmTemperature())
                        .build()
        );
        sseAskParams.setSseEmitter(sseEmitter);
        sseAskParams.setModelName(aiModel.getName());
        sseAskParams.setUser(user);
        // 无可用召回数量时，根据严格/宽松模式决定是否直接返回错误
        if (maxResults == 0) {
            log.info("用户问题过长，无需再召回文档，严格模式下直接返回异常提示,宽松模式下接着请求LLM");
            if (Boolean.TRUE.equals(knowledgeBase.getIsStrict())) {
                // 严格模式下直接结束 SSE，避免继续消耗资源
                sseEmitterHelper.sendErrorAndComplete(user.getId(), sseEmitter, "提问内容过长，最多不超过 " + maxInputTokens + " tokens");
                // 清理线程变量，避免污染后续请求
                TokenEstimatorThreadLocal.clearTokenEstimator();
            } else {
                // 宽松模式下直接调用模型生成答案，仍然回写记录
                sseEmitterHelper.call(sseAskParams, (response, questionMeta, answerMeta) -> {
                            sseEmitterHelper.sendComplete(user.getId(), sseEmitter);
                            // 回写问答记录与计费信息，保持统计一致
                            updateQaRecord(
                                    UpdateQaParams.builder()
                                            .user(user)
                                            .qaRecord(qaRecord)
                                            .retrievers(null)
                                            .sseAskParams(sseAskParams)
                                            .response(response.getContent())
                                            .isTokenFree(aiModel.getIsFree())
                                            .build());
                            // 清理线程变量，避免污染后续请求
                            TokenEstimatorThreadLocal.clearTokenEstimator();
                        }
                );
            }
        } else {
            log.info("进行RAG请求,maxResults:{}", maxResults);
            // 使用知识库配置的模型构建检索器，确保与索引一致
            ChatModel chatModel = LLMContext.getServiceById(knowledgeBase.getIngestModelId(), true).buildChatLLM(
                    ChatModelBuilderProperties.builder()
                            .temperature(knowledgeBase.getQueryLlmTemperature())
                            .build());
            // 仅检索当前知识库，避免跨库召回影响回答
            RetrieverCreateParam createParam = RetrieverCreateParam.builder()
                    .chatModel(chatModel)
                    .filter(new IsEqualTo(AdiConstant.MetadataKey.KB_UUID, qaRecord.getKbUuid()))
                    .maxResults(maxResults)
                    .minScore(knowledgeBase.getRetrieveMinScore())
                    .breakIfSearchMissed(knowledgeBase.getIsStrict())
                    .build();
            CompositeRag compositeRag = new CompositeRag(KNOWLEDGE_BASE);
            List<RetrieverWrapper> retrieverWrappers = compositeRag.createRetriever(createParam);
            List<ContentRetriever> retrievers = retrieverWrappers.stream().map(RetrieverWrapper::getRetriever).toList();
            // 组合检索执行 RAG，并在回调中完成收尾与统计
            compositeRag.ragChat(retrievers, sseAskParams, (response, promptMeta, answerMeta) -> {
                        sseEmitterHelper.sendComplete(user.getId(), sseAskParams.getSseEmitter());
                        // 回写问答记录、引用与成本，保证可追溯
                        updateQaRecord(
                                UpdateQaParams.builder()
                                        .user(user)
                                        .qaRecord(qaRecord)
                                        .retrievers(retrievers)
                                        .sseAskParams(sseAskParams)
                                        .response(response)
                                        .isTokenFree(aiModel.getIsFree())
                                        .build());
                        // 清理线程变量，避免污染后续请求
                        TokenEstimatorThreadLocal.clearTokenEstimator();
                    }
            );
        }
    }

    /**
     * 更新问答记录并写入消耗统计。
     *
     * @param updateQaParams 更新参数
     * @return 无
     */
    private void updateQaRecord(UpdateQaParams updateQaParams) {

        // 从 Redis 汇总本次请求的 token 消耗，保证计费与统计一致
        Pair<Integer, Integer> inputOutputTokenCost = LLMTokenUtil.calAllTokenCostByUuid(stringRedisTemplate, updateQaParams.getSseAskParams().getUuid());

        KnowledgeBaseQa qaRecord = updateQaParams.getQaRecord();
        User user = updateQaParams.getUser();

        // 只更新必要字段，避免覆盖未变更数据
        KnowledgeBaseQa updateRecord = new KnowledgeBaseQa();
        updateRecord.setId(qaRecord.getId());
        updateRecord.setPrompt(updateQaParams.getSseAskParams().getHttpRequestParams().getUserMessage());
        updateRecord.setPromptTokens(inputOutputTokenCost.getLeft());
        updateRecord.setAnswer(updateQaParams.getResponse());
        updateRecord.setAnswerTokens(inputOutputTokenCost.getRight());
        knowledgeBaseQaRecordService.updateById(updateRecord);

        // 创建引用并写入成本统计，便于后续溯源与计费
        createRef(updateQaParams.getRetrievers(), user, qaRecord.getId());
        // 用户本次请求消耗的 token 数指的是整个 RAG 过程中消耗的 token 数量，其中可能涉及多次 LLM 请求
        int allToken = inputOutputTokenCost.getLeft() + inputOutputTokenCost.getRight();
        log.info("用户{}本次请示消耗总token:{}", user.getName(), allToken);
        if (allToken > 0) {
            userDayCostService.appendCostToUser(user, allToken, updateQaParams.isTokenFree());
        }
    }

    /**
     * 创建引用记录
     *
     * @param retrievers 召回器
     * @param user       用户
     * @param qaId       问答id
     * @return 无
     */
    private void createRef(List<ContentRetriever> retrievers, User user, Long qaId) {
        // 无召回器时无需写入引用，避免无意义的数据库操作
        if (CollectionUtils.isEmpty(retrievers)) {
            return;
        }
        for (ContentRetriever retriever : retrievers) {
            // 根据召回器类型写入不同的引用，保证溯源结构清晰
            if (retriever instanceof AdiEmbeddingStoreContentRetriever embeddingRetriever) {
                knowledgeBaseQaRecordService.createEmbeddingRefs(user, qaId, embeddingRetriever.getRetrievedEmbeddingToScore());
            } else if (retriever instanceof GraphStoreContentRetriever graphRetriever) {
                knowledgeBaseQaRecordService.createGraphRefs(user, qaId, graphRetriever.getGraphRef());
            }
        }
    }

    /**
     * 获取知识库，不存在则抛异常。
     *
     * @param kbUuid 知识库 UUID
     * @return 知识库实体
     */
    public KnowledgeBase getOrThrow(String kbUuid) {
        return ChainWrappers.lambdaQueryChain(baseMapper)
                .eq(KnowledgeBase::getUuid, kbUuid)
                .eq(KnowledgeBase::getIsDeleted, false)
                .oneOpt().orElseThrow(() -> new BaseException(A_DATA_NOT_FOUND));
    }

    /**
     * 标记需要更新知识库统计。
     *
     * @param kbUuid 知识库uuid
     */
    public void updateStatistic(String kbUuid) {
        stringRedisTemplate.opsForSet().add(KB_STATISTIC_RECALCULATE_SIGNAL, kbUuid);
    }

    /**
     * 统计当天创建的知识库数量。
     *
     * @return 数量
     */
    public int countTodayCreated() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime beginTime = LocalDateTime.of(now.getYear(), now.getMonth(), now.getDayOfMonth(), 0, 0, 0);
        LocalDateTime endTime = beginTime.plusDays(1);
        return baseMapper.countCreatedByTimePeriod(beginTime, endTime);
    }

    /**
     * 统计全部知识库数量。
     *
     * @return 数量
     */
    public int countAllCreated() {
        return baseMapper.countAllCreated();
    }

    /**
     * 定时更新知识库统计。
     */
    @Scheduled(fixedDelay = 60 * 1000)
    public void asyncUpdateStatistic() {
        Set<String> kbUuidList = stringRedisTemplate.opsForSet().members(KB_STATISTIC_RECALCULATE_SIGNAL);
        if (CollectionUtils.isEmpty(kbUuidList)) {
            return;
        }
        for (String kbUuid : kbUuidList) {
            int embeddingCount = embeddingService.countByKbUuid(kbUuid);
            baseMapper.updateStatByUuid(kbUuid, embeddingCount);
            stringRedisTemplate.opsForSet().remove(KB_STATISTIC_RECALCULATE_SIGNAL, kbUuid);
        }
    }

    /**
     * 校验用户对知识库的权限。
     *
     * @param kbId   知识库 ID
     * @param kbUuid 知识库 UUID
     */
    private void checkPrivilege(Long kbId, String kbUuid) {
        if (null == kbId && StringUtils.isBlank(kbUuid)) {
            throw new BaseException(A_PARAMS_ERROR);
        }
        User user = ThreadContext.getCurrentUser();
        if (null == user) {
            throw new BaseException(A_USER_NOT_EXIST);
        }
        boolean privilege = user.getIsAdmin();
        if (privilege) {
            return;
        }
        LambdaQueryWrapper<KnowledgeBase> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KnowledgeBase::getOwnerId, user.getId());
        if (null != kbId) {
            wrapper = wrapper.eq(KnowledgeBase::getId, kbId);
        } else if (StringUtils.isNotBlank(kbUuid)) {
            wrapper = wrapper.eq(KnowledgeBase::getUuid, kbUuid);
        }
        boolean exists = baseMapper.exists(wrapper);
        if (!exists) {
            throw new BaseException(A_USER_NOT_AUTH);
        }
    }

}
