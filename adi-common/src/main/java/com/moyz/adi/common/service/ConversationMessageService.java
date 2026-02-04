package com.moyz.adi.common.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.moyz.adi.common.base.ThreadContext;
import com.moyz.adi.common.cosntant.AdiConstant;
import com.moyz.adi.common.dto.AskReq;
import com.moyz.adi.common.dto.KbInfoResp;
import com.moyz.adi.common.dto.RefGraphDto;
import com.moyz.adi.common.entity.*;
import com.moyz.adi.common.enums.ChatMessageRoleEnum;
import com.moyz.adi.common.enums.ErrorEnum;
import com.moyz.adi.common.exception.BaseException;
import com.moyz.adi.common.file.FileOperatorContext;
import com.moyz.adi.common.file.LocalFileUtil;
import com.moyz.adi.common.helper.AsrModelContext;
import com.moyz.adi.common.helper.LLMContext;
import com.moyz.adi.common.helper.QuotaHelper;
import com.moyz.adi.common.helper.SSEEmitterHelper;
import com.moyz.adi.common.languagemodel.data.LLMResponseContent;
import com.moyz.adi.common.mapper.ConversationMessageMapper;
import com.moyz.adi.common.memory.longterm.LongTermMemoryService;
import com.moyz.adi.common.memory.shortterm.MapDBChatMemoryStore;
import com.moyz.adi.common.rag.AdiEmbeddingStoreContentRetriever;
import com.moyz.adi.common.rag.CompositeRag;
import com.moyz.adi.common.rag.GraphStoreContentRetriever;
import com.moyz.adi.common.languagemodel.AbstractLLMService;
import com.moyz.adi.common.util.*;
import com.moyz.adi.common.vo.*;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.query.Query;
import dev.langchain4j.store.embedding.filter.comparison.IsEqualTo;
import dev.langchain4j.store.embedding.filter.comparison.IsIn;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import ws.schild.jave.info.MultimediaInfo;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.moyz.adi.common.cosntant.AdiConstant.*;
import static com.moyz.adi.common.cosntant.AdiConstant.MetadataKey.CONVERSATION_ID;
import static com.moyz.adi.common.cosntant.AdiConstant.MetadataKey.KB_UUID;
import static com.moyz.adi.common.enums.ErrorEnum.A_CONVERSATION_NOT_FOUND;
import static com.moyz.adi.common.enums.ErrorEnum.B_MESSAGE_NOT_FOUND;
import static com.moyz.adi.common.util.AdiStringUtil.stringToList;

/**
 * 对话消息处理服务。
 */
@Slf4j
@Service
public class ConversationMessageService extends ServiceImpl<ConversationMessageMapper, ConversationMessage> {

    /**
     * 自身代理对象（用于触发异步方法）。
     */
    @Lazy
    @Resource
    private ConversationMessageService self;

    /**
     * 配额校验辅助。
     */
    @Resource
    private QuotaHelper quotaHelper;

    /**
     * 用户日消耗统计服务。
     */
    @Resource
    private UserDayCostService userDayCostService;

    /**
     * 对话服务。
     */
    @Lazy
    @Resource
    private ConversationService conversationService;

    /**
     * 消息引用的向量服务。
     */
    @Resource
    private ConversationMessageRefEmbeddingService conversationMessageRefEmbeddingService;

    /**
     * 消息引用的图谱服务。
     */
    @Resource
    private ConversationMessageRefGraphService conversationMessageRefGraphService;

    /**
     * 用户 MCP 服务。
     */
    @Resource
    private UserMcpService userMcpService;

    /**
     * SSE 发送辅助。
     */
    @Resource
    private SSEEmitterHelper sseEmitterHelper;

    /**
     * 文件服务。
     */
    @Resource
    private FileService fileService;

    /**
     * 主线程执行器。
     */
    @Resource
    private AsyncTaskExecutor mainExecutor;

    /**
     * 长期记忆服务。
     */
    @Resource
    private LongTermMemoryService longTermMemoryService;

    /**
     * 以 SSE 方式发起提问请求。
     *
     * @param askReq 提问请求
     * @return SSE 连接
     */
    public SseEmitter sseAsk(AskReq askReq) {
        SseEmitter sseEmitter = new SseEmitter(SSE_TIMEOUT);
        User user = ThreadContext.getCurrentUser();
        if (!sseEmitterHelper.checkOrComplete(user, sseEmitter)) {
            return sseEmitter;
        }
        sseEmitterHelper.startSse(user, sseEmitter);
        self.asyncCheckAndChat(sseEmitter, user, askReq);
        return sseEmitter;
    }

    /**
     * 校验对话是否合法以及配额是否允许。
     *
     * @param sseEmitter SSE 连接
     * @param user       用户
     * @param askReq     提问请求
     * @return 是否允许继续
     */
    private boolean checkConversation(SseEmitter sseEmitter, User user, AskReq askReq) {
        try {

            // 校验 1：对话是否已被删除
            Conversation delConv = conversationService.lambdaQuery()
                    .eq(Conversation::getUuid, askReq.getConversationUuid())
                    .eq(Conversation::getIsDeleted, true)
                    .one();
            if (null != delConv) {
                sseEmitterHelper.sendErrorAndComplete(user.getId(), sseEmitter, "该对话已经删除");
                return false;
            }

            // 校验 2：对话数量配额
            Long convsCount = conversationService.lambdaQuery()
                    .eq(Conversation::getUserId, user.getId())
                    .eq(Conversation::getIsDeleted, false)
                    .count();
            long convsMax = Integer.parseInt(LocalCache.CONFIGS.get(AdiConstant.SysConfigKey.CONVERSATION_MAX_NUM));
            if (convsCount >= convsMax) {
                sseEmitterHelper.sendErrorAndComplete(user.getId(), sseEmitter, "对话数量已经达到上限，当前对话上限为：" + convsMax);
                return false;
            }

            // 校验 3：当前用户配额
            AiModel aiModel = LLMContext.getAiModel(askReq.getModelPlatform(), askReq.getModelName());
            if (null != aiModel && !aiModel.getIsFree()) {
                ErrorEnum errorMsg = quotaHelper.checkTextQuota(user);
                if (null != errorMsg) {
                    sseEmitterHelper.sendErrorAndComplete(user.getId(), sseEmitter, errorMsg.getInfo());
                    return false;
                }
            }
        } catch (Exception e) {
            log.error("error", e);
            sseEmitter.completeWithError(e);
            return false;
        }
        return true;
    }

    /**
     * 异步校验并发起对话请求。
     *
     * @param sseEmitter SSE 连接
     * @param user       用户
     * @param askReq     提问请求
     */
    @Async
    public void asyncCheckAndChat(SseEmitter sseEmitter, User user, AskReq askReq) {
        log.info("asyncCheckAndChat,userId:{}", user.getId());
        // 校验业务规则
        if (!checkConversation(sseEmitter, user, askReq)) {
            return;
        }
        // 获取对话信息
        Conversation conversation = conversationService.lambdaQuery()
                .eq(Conversation::getUuid, askReq.getConversationUuid())
                .oneOpt()
                .orElse(null);
        if (null == conversation) {
            sseEmitterHelper.sendErrorAndComplete(user.getId(), sseEmitter, A_CONVERSATION_NOT_FOUND.getInfo());
            return;
        }

        // 发送“问题分析中”状态给前端
        SSEEmitterHelper.sendPartial(sseEmitter, SSEEventName.STATE_CHANGED, SSEEventData.STATE_QUESTION_ANALYSING);
        //如果是语音输入，将音频转成文本
        if (StringUtils.isNotBlank(askReq.getAudioUuid())) {
            String path = fileService.getImagePath(askReq.getAudioUuid());
            String audioText = new AsrModelContext().audioToText(path);
            if (StringUtils.isBlank(audioText)) {
                sseEmitterHelper.sendErrorAndComplete(user.getId(), sseEmitter, "音频解析失败，请检查音频文件是否正确");
                return;
            }
            askReq.setPrompt(audioText);
        }

        AbstractLLMService llmService = LLMContext.getServiceOrDefault(askReq.getModelPlatform(), askReq.getModelName());

        // 如果关联了知识库，筛选出有效的知识库以待后续查询，同时通知前端开始搜索
        List<KbInfoResp> filteredKb = new ArrayList<>();
        if (StringUtils.isNotBlank(conversation.getKbIds())) {
            List<Long> kbIds = Arrays.stream(conversation.getKbIds().split(",")).map(Long::parseLong).toList();
            filteredKb = conversationService.filterEnableKb(user, kbIds);

            // 发送“知识库检索中”状态给前端
            SSEEmitterHelper.sendPartial(sseEmitter, SSEEventName.STATE_CHANGED, SSEEventData.STATE_KNOWLEDGE_SEARCHING);
        }
        // 从知识库与会话记忆中检索内容
        List<RetrieverWrapper> retrieverWrappers = retrieve(conversation.getId(), filteredKb, llmService, askReq);

        // 根据检索内容与音频配置加工提示词
        int answerContentType = getAnswerContentType(conversation, askReq);
        boolean answerToAudio = TtsUtil.needTts(llmService.getTtsSetting(), answerContentType);
        Pair<String, String> memoryAndKnowledge = buildMemoryAndKnowledge(retrieverWrappers);
        String processedPrompt = PromptUtil.createPrompt(askReq.getPrompt(), memoryAndKnowledge.getLeft(), memoryAndKnowledge.getRight(), answerToAudio ? PROMPT_EXTRA_AUDIO : "");
        if (!Objects.equals(askReq.getPrompt(), processedPrompt)) {
            askReq.setProcessedPrompt(processedPrompt);
        }

        String questionUuid = StringUtils.isNotBlank(askReq.getRegenerateQuestionUuid()) ? askReq.getRegenerateQuestionUuid() : UuidUtil.createShort();
        SseAskParams sseAskParams = new SseAskParams();
        sseAskParams.setUser(user);
        sseAskParams.setUuid(questionUuid);
        sseAskParams.setModelName(askReq.getModelName());
        sseAskParams.setSseEmitter(sseEmitter);
        sseAskParams.setRegenerateQuestionUuid(askReq.getRegenerateQuestionUuid());
        sseAskParams.setAnswerContentType(answerContentType);
        if (null != conversation.getAudioConfig() && null != conversation.getAudioConfig().getVoice()) {
            //如果对话配置了语音，则使用对话的语音配置
            sseAskParams.setVoice(conversation.getAudioConfig().getVoice().getParamName());
        }

        ChatModelRequestParams chatRequestParams = buildChatRequestParams(conversation, askReq);
        sseAskParams.setHttpRequestParams(chatRequestParams);

        sseAskParams.setModelProperties(
                ChatModelBuilderProperties.builder()
                        .temperature(conversation.getLlmTemperature())
                        .build()
        );
        sseEmitterHelper.call(sseAskParams, (response, questionMeta, answerMeta) -> {

            AudioInfo audioInfo = null;
            if (StringUtils.isNotBlank(response.getAudioPath())) {

                audioInfo = new AudioInfo();
                MultimediaInfo multimediaInfo = LocalFileUtil.getAudioFileInfo(response.getAudioPath());
                if (null != multimediaInfo) {
                    audioInfo.setDuration((int) multimediaInfo.getDuration() / 1000);
                }
                audioInfo.setPath(response.getAudioPath());
                // 存储到数据库并返回真实的 URL
                AdiFile adiFile = fileService.saveFromPath(user, response.getAudioPath());
                audioInfo.setUuid(adiFile.getUuid());
                audioInfo.setUrl(FileOperatorContext.getFileUrl(adiFile));
            }
            boolean isRefEmbedding = false;
            boolean isRefGraph = false;
            for (RetrieverWrapper wrapper : retrieverWrappers) {
                // 待办：记忆相关的引用也要存储到数据库
                if (!RetrieveContentFrom.KNOWLEDGE_BASE.equals(wrapper.getContentFrom())) {
                    continue;
                }
                if (wrapper.getRetriever() instanceof AdiEmbeddingStoreContentRetriever embeddingStoreContentRetriever) {
                    isRefEmbedding = !embeddingStoreContentRetriever.getRetrievedEmbeddingToScore().isEmpty();
                } else if (wrapper.getRetriever() instanceof GraphStoreContentRetriever graphStoreContentRetriever) {
                    RefGraphDto graphDto = graphStoreContentRetriever.getGraphRef();
                    isRefGraph = !graphDto.getVertices().isEmpty() || !graphDto.getEdges().isEmpty();
                }
            }
            answerMeta.setIsRefEmbedding(isRefEmbedding);
            answerMeta.setIsRefGraph(isRefGraph);
            sseEmitterHelper.sendComplete(user.getId(), sseEmitter, questionMeta, answerMeta, audioInfo);
            self.saveAfterAiResponse(user, askReq, retrieverWrappers, response, questionMeta, answerMeta, audioInfo);
        });
    }

    /**
     * 判断是否需要返回推理过程
     * 以下两种场景表示需要返回推理过程
     * 场景1：模型是推理模型 && 不允许关闭推理过程，
     * 场景2：模型是推理模型 && 允许关闭推理过程 && 角色会话开启了深度思考
     *
     * @param aiModel      模型
     * @param conversation 会话
     * @return 是否需要返回推理过程
     */
    private Boolean checkIfReturnThinking(AiModel aiModel, Conversation conversation) {
        if (!aiModel.getIsReasoner()) {
            return null;
        }
        return Boolean.FALSE.equals(aiModel.getIsThinkingClosable()) || Boolean.TRUE.equals(conversation.getIsEnableThinking());
    }

    /**
     * 多知识库搜索、记忆搜索
     *
     * @param convId     会话id
     * @param filteredKb 有效的已关联的知识库
     * @param llmService 大模型服务
     * @param askReq     请求参数
     */
    private List<RetrieverWrapper> retrieve(Long convId, List<KbInfoResp> filteredKb, AbstractLLMService llmService, AskReq askReq) {
        ChatModel chatModel = llmService.buildChatLLM(
                ChatModelBuilderProperties.builder()
                        .temperature(LLM_TEMPERATURE_DEFAULT)
                        .build());
        // 创建记忆检索器
        RetrieverCreateParam memoryRetrieveParam = RetrieverCreateParam.builder()
                .chatModel(chatModel)
                .filter(new IsEqualTo(CONVERSATION_ID, convId))
                .maxResults(3)
                .minScore(RAG_RETRIEVE_MIN_SCORE_DEFAULT)
                .breakIfSearchMissed(false)
                .build();
        List<RetrieverWrapper> retrieverWrappers = new CompositeRag(RetrieveContentFrom.CONV_MEMORY).createRetriever(memoryRetrieveParam);
        // 创建知识库检索器
        if (!filteredKb.isEmpty()) {
            List<String> kbUuids = filteredKb.stream().map(KbInfoResp::getUuid).toList();
            log.info("准备搜索相关联的知识库,kbUuids:{},question:{}", String.join(",", kbUuids), askReq.getPrompt());
            //忽略知识库自身的设置如 [最大召回数量maxResult，最小命中分数minScore，角色设置，是否强行中断搜索] 等
            RetrieverCreateParam kbRetrieveParam = RetrieverCreateParam.builder()
                    .chatModel(chatModel)
                    .filter(new IsIn(KB_UUID, kbUuids)) //跨多个知识库查询
                    .maxResults(3)
                    .minScore(RAG_RETRIEVE_MIN_SCORE_DEFAULT)
                    .breakIfSearchMissed(false)
                    .build();
            List<RetrieverWrapper> kbRetrievers = new CompositeRag(RetrieveContentFrom.KNOWLEDGE_BASE).createRetriever(kbRetrieveParam);
            retrieverWrappers.addAll(kbRetrievers);
        }
        // 并发检索内容
        if (!retrieverWrappers.isEmpty()) {
            CountDownLatch countDownLatch = new CountDownLatch(retrieverWrappers.size());
            for (RetrieverWrapper retriever : retrieverWrappers) {
                mainExecutor.execute(() -> {
                    try {
                        List<Content> contents = retriever.getRetriever().retrieve(Query.from(askReq.getPrompt()));
                        retriever.setResponse(contents);
                    } catch (Exception e) {
                        log.error("检索内容失败", e);
                    } finally {
                        countDownLatch.countDown();
                    }
                });
            }
            try {
                boolean awaitRet = countDownLatch.await(1, TimeUnit.MINUTES);
                if (!awaitRet) {
                    log.warn("检索等待超时");
                }
            } catch (InterruptedException e) {
                log.error("检索等待异常", e);
                Thread.currentThread().interrupt();
            }
        }
        return retrieverWrappers;
    }

    /**
     * 查询会话中的问题消息列表。
     *
     * @param convId   会话 ID
     * @param maxId    最大消息 ID
     * @param pageSize 页大小
     * @return 消息列表
     */
    public List<ConversationMessage> listQuestionsByConvId(long convId, long maxId, int pageSize) {
        LambdaQueryWrapper<ConversationMessage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ConversationMessage::getConversationId, convId);
        queryWrapper.eq(ConversationMessage::getParentMessageId, 0);
        queryWrapper.lt(ConversationMessage::getId, maxId);
        queryWrapper.eq(ConversationMessage::getIsDeleted, false);
        queryWrapper.last("limit " + pageSize);
        queryWrapper.orderByDesc(ConversationMessage::getId);
        return getBaseMapper().selectList(queryWrapper);
    }

    /**
     * 在模型回复后保存消息、引用与消耗统计。
     *
     * @param user        用户
     * @param askReq      提问请求
     * @param retrievers  检索器列表
     * @param response    回复内容
     * @param questionMeta 问题元信息
     * @param answerMeta  答案元信息
     * @param audioInfo   音频信息
     */
    @Transactional
    public void saveAfterAiResponse(User user, AskReq askReq, List<RetrieverWrapper> retrievers, LLMResponseContent response, PromptMeta questionMeta, AnswerMeta answerMeta, AudioInfo audioInfo) {
        Conversation conversation;
        String prompt = askReq.getPrompt();
        String convUuid = askReq.getConversationUuid();
        String modelPlatform = askReq.getModelPlatform();
        String modelName = askReq.getModelName();
        conversation = conversationService.lambdaQuery()
                .eq(Conversation::getUuid, convUuid)
                .eq(Conversation::getUserId, user.getId())
                .oneOpt()
                .orElseGet(() -> conversationService.createByFirstMessage(user.getId(), convUuid, prompt));
        AiModel aiModel = LLMContext.getAiModel(modelPlatform, modelName);
        // 判断是否为重新生成的问题
        ConversationMessage promptMsg;
        if (StringUtils.isNotBlank(askReq.getRegenerateQuestionUuid())) {
            promptMsg = getPromptMsgByQuestionUuid(askReq.getRegenerateQuestionUuid());
        } else {
            // 保存新的问题消息
            ConversationMessage question = new ConversationMessage();
            question.setUserId(user.getId());
            question.setUuid(questionMeta.getUuid());
            question.setConversationId(conversation.getId());
            question.setConversationUuid(convUuid);
            question.setMessageRole(ChatMessageRoleEnum.USER.getValue());
            question.setRemark(prompt);
            question.setProcessedRemark(askReq.getProcessedPrompt());
            question.setAiModelId(aiModel.getId());
            question.setAudioUuid(askReq.getAudioUuid());
            question.setAudioDuration(askReq.getAudioDuration());
            question.setTokens(questionMeta.getTokens());
            question.setUnderstandContextMsgPairNum(user.getUnderstandContextMsgPairNum());
            question.setAttachments(String.join(",", askReq.getImageUrls()));
            baseMapper.insert(question);

            promptMsg = this.lambdaQuery().eq(ConversationMessage::getUuid, questionMeta.getUuid()).one();
        }

        // 保存回复消息
        ConversationMessage aiAnswer = new ConversationMessage();
        aiAnswer.setUserId(user.getId());
        aiAnswer.setUuid(answerMeta.getUuid());
        aiAnswer.setConversationId(conversation.getId());
        aiAnswer.setConversationUuid(convUuid);
        aiAnswer.setMessageRole(ChatMessageRoleEnum.ASSISTANT.getValue());
        aiAnswer.setThinkingContent(Objects.toString(response.getThinkingContent(), ""));
        aiAnswer.setRemark(response.getContent());
        // 待办：过滤或转换 AI 返回的内容
        aiAnswer.setAudioUuid(null == audioInfo ? "" : Objects.toString(audioInfo.getUuid(), ""));
        aiAnswer.setAudioDuration(null == audioInfo ? 0 : audioInfo.getDuration());
        aiAnswer.setTokens(answerMeta.getTokens());
        aiAnswer.setParentMessageId(promptMsg.getId());
        aiAnswer.setAiModelId(aiModel.getId());
        aiAnswer.setIsRefEmbedding(answerMeta.getIsRefEmbedding());
        aiAnswer.setIsRefGraph(answerMeta.getIsRefGraph());
        int answerContentType = getAnswerContentType(conversation, askReq);
        aiAnswer.setContentType(answerContentType);
        baseMapper.insert(aiAnswer);

        createRef(retrievers, user, aiAnswer.getId());

        calcTodayCost(user, conversation, questionMeta, answerMeta, aiModel.getIsFree());

        // 写入短期记忆
        if (Boolean.TRUE.equals(conversation.getUnderstandContextEnable())) {
            MapDBChatMemoryStore mapDBChatMemoryStore = MapDBChatMemoryStore.getSingleton();
            List<ChatMessage> messages = mapDBChatMemoryStore.getMessages(askReq.getConversationUuid());
            List<ChatMessage> newMessages = new ArrayList<>(messages);
            newMessages.add(AiMessage.aiMessage(response.getContent()));
            mapDBChatMemoryStore.updateMessages(askReq.getConversationUuid(), newMessages);
        }

        // 待办：部分视觉模型如 qwen2-vl-7b-instruct 不支持 JSON 结构返回内容，待处理
        if (!aiModel.getType().equalsIgnoreCase(ModelType.VISION)) {
            // 写入长期记忆
            longTermMemoryService.asyncAdd(conversation.getId(), modelPlatform, modelName, askReq.getPrompt(), response.getContent());
            // 待办：异步计算 token 消耗并更新用户日统计（包含长期记忆分析消耗）
        }
    }

    /**
     * 统计并累计当天的 token 消耗。
     *
     * @param user         用户
     * @param conversation 会话
     * @param questionMeta 问题元信息
     * @param answerMeta   答案元信息
     * @param isFreeToken  是否免费额度
     */
    private void calcTodayCost(User user, Conversation conversation, PromptMeta questionMeta, AnswerMeta answerMeta, boolean isFreeToken) {

        int todayTokenCost = questionMeta.getTokens() + answerMeta.getTokens();
        try {
            // 计算会话累计 token
            conversationService.lambdaUpdate()
                    .eq(Conversation::getId, conversation.getId())
                    .set(Conversation::getTokens, conversation.getTokens() + todayTokenCost)
                    .update();

            userDayCostService.appendCostToUser(user, todayTokenCost, isFreeToken);
        } catch (Exception e) {
            log.error("calcTodayCost error", e);
        }
    }

    /**
     * 通过问题 UUID 获取对应消息。
     *
     * @param questionUuid 问题 UUID
     * @return 消息实体
     */
    private ConversationMessage getPromptMsgByQuestionUuid(String questionUuid) {
        return this.lambdaQuery().eq(ConversationMessage::getUuid, questionUuid).oneOpt().orElseThrow(() -> new BaseException(B_MESSAGE_NOT_FOUND));
    }

    /**
     * 软删除消息。
     *
     * @param uuid 消息 UUID
     * @return 是否删除成功
     */
    public boolean softDelete(String uuid) {
        return this.lambdaUpdate()
                .eq(ConversationMessage::getUuid, uuid)
                .eq(ConversationMessage::getUserId, ThreadContext.getCurrentUserId())
                .eq(ConversationMessage::getIsDeleted, false)
                .set(ConversationMessage::getIsDeleted, true)
                .update();
    }

    /**
     * 根据音频 UUID 获取文本内容。
     *
     * @param audioUuid 音频 UUID
     * @return 文本内容
     */
    public String getTextByAudioUuid(String audioUuid) {
        if (StringUtils.isBlank(audioUuid)) {
            return null;
        }
        ConversationMessage conversationMessage = this.lambdaQuery()
                .eq(ConversationMessage::getAudioUuid, audioUuid)
                .eq(!ThreadContext.getCurrentUser().getIsAdmin(), ConversationMessage::getUserId, ThreadContext.getCurrentUserId())
                .eq(ConversationMessage::getIsDeleted, false)
                .last("limit 1")
                .oneOpt()
                .orElse(null);
        if (null == conversationMessage) {
            return null;
        }
        return conversationMessage.getRemark();
    }

    /**
     * 创建消息引用记录（向量或图谱）。
     *
     * @param wrappers 检索器列表
     * @param user     用户
     * @param msgId    消息 ID
     */
    private void createRef(List<RetrieverWrapper> wrappers, User user, Long msgId) {
        if (CollectionUtils.isEmpty(wrappers)) {
            return;
        }
        // 待办：记忆相关的引用也要存储到数据库
        for (RetrieverWrapper wrapper : wrappers) {
            if (!RetrieveContentFrom.KNOWLEDGE_BASE.equals(wrapper.getContentFrom())) {
                continue;
            }
            if (wrapper.getRetriever() instanceof AdiEmbeddingStoreContentRetriever embeddingRetriever) {
                self.createEmbeddingRefs(user, msgId, embeddingRetriever.getRetrievedEmbeddingToScore());
            } else if (wrapper.getRetriever() instanceof GraphStoreContentRetriever graphRetriever) {
                self.createGraphRefs(user, msgId, graphRetriever.getGraphRef());
            }
        }
    }

    /**
     * 增加嵌入引用记录
     *
     * @param user             用户
     * @param messageId        消息id
     * @param embeddingToScore 嵌入向量id和分数的映射
     */
    public void createEmbeddingRefs(User user, Long messageId, Map<String, Double> embeddingToScore) {
        log.info("创建向量引用,userId:{},qaRecordId:{},embeddingToScore.size:{}", user.getId(), messageId, embeddingToScore.size());
        for (Map.Entry<String, Double> entry : embeddingToScore.entrySet()) {
            String embeddingId = entry.getKey();
            ConversationMessageRefEmbedding recordReference = new ConversationMessageRefEmbedding();
            recordReference.setMessageId(messageId);
            recordReference.setEmbeddingId(embeddingId);
            recordReference.setScore(embeddingToScore.get(embeddingId));
            recordReference.setUserId(user.getId());
            conversationMessageRefEmbeddingService.save(recordReference);
        }
    }

    /**
     * 增加图谱引用记录
     *
     * @param user      用户
     * @param messageId 消息id
     * @param graphDto  图谱引用数据传输对象
     */
    public void createGraphRefs(User user, Long messageId, RefGraphDto graphDto) {
        log.info("准备创建图谱引用,userId:{},qaRecordId:{},vertices.Size:{},edges.size:{}", user.getId(), messageId, graphDto.getVertices().size(), graphDto.getEdges().size());
        if (graphDto.getVertices().isEmpty() && graphDto.getEdges().isEmpty()) {
            log.warn("图谱引用数据为空，无法创建图谱引用记录,userId:{},qaRecordId:{}", user.getId(), messageId);
            return;
        }
        String entities = null == graphDto.getEntitiesFromQuestion() ? "" : String.join(",", graphDto.getEntitiesFromQuestion());
        Map<String, Object> graphFromStore = new HashMap<>();
        graphFromStore.put("vertices", graphDto.getVertices());
        graphFromStore.put("edges", graphDto.getEdges());
        ConversationMessageRefGraph refGraph = new ConversationMessageRefGraph();
        refGraph.setMessageId(messageId);
        refGraph.setUserId(user.getId());
        refGraph.setGraphFromLlm(entities);
        refGraph.setGraphFromStore(JsonUtil.toJson(graphFromStore));
        conversationMessageRefGraphService.save(refGraph);
    }

    /**
     * 构建对话模型请求参数。
     *
     * @param conversation 对话
     * @param askReq       请求参数
     * @return 请求参数对象
     */
    private ChatModelRequestParams buildChatRequestParams(Conversation conversation, AskReq askReq) {
        ChatModelRequestParams.ChatModelRequestParamsBuilder builder = ChatModelRequestParams.builder();
        if (StringUtils.isNotBlank(conversation.getAiSystemMessage())) {
            builder.systemMessage(conversation.getAiSystemMessage());
        }
        // 是否启用历史消息
        if (Boolean.TRUE.equals(conversation.getUnderstandContextEnable())) {
            builder.memoryId(askReq.getConversationUuid());
        }
        //如果用户问题已处理过，例如增加了召回的文档文段，则使用该增强的问题，否则使用用户的原始问题
        String prompt = StringUtils.isNotBlank(askReq.getProcessedPrompt()) ? askReq.getProcessedPrompt() : askReq.getPrompt();
        if (StringUtils.isNotBlank(askReq.getRegenerateQuestionUuid())) {
            ConversationMessage lastMsg = getPromptMsgByQuestionUuid(askReq.getRegenerateQuestionUuid());
            prompt = StringUtils.isNotBlank(lastMsg.getProcessedRemark()) ? lastMsg.getProcessedRemark() : lastMsg.getRemark();
        }
        builder.userMessage(prompt);
        builder.imageUrls(askReq.getImageUrls());

        List<McpClient> mcpClients = new ArrayList<>();
        if (StringUtils.isNotBlank(conversation.getMcpIds())) {
            List<Long> mcpIds = stringToList(conversation.getMcpIds(), ",", Long::parseLong);
            mcpClients = userMcpService.createMcpClients(conversation.getUserId(), mcpIds);
        }
        builder.mcpClients(mcpClients);

        // 是否开启思考过程
        AiModel aiModel = LLMContext.getServiceOrDefault(askReq.getModelPlatform(), askReq.getModelName()).getAiModel();
        Boolean returnThinking = checkIfReturnThinking(aiModel, conversation);
        builder.returnThinking(returnThinking);

        // 是否开启网页搜索
        builder.enableWebSearch(Boolean.TRUE.equals(conversation.getIsEnableWebSearch()));

        return builder.build();
    }

    /**
     * 获取响应内容类型
     *
     * @param conversation 对话
     * @param askReq       请求参数
     * @return 响应内容类型
     */
    private int getAnswerContentType(Conversation conversation, AskReq askReq) {
        int answerContentType = conversation.getAnswerContentType();
        //如果设置了响应内容类型为自动，并且用户输入是音频，则响应内容类型设置为音频
        if (answerContentType == AdiConstant.ConversationConstant.ANSWER_CONTENT_TYPE_AUTO && StringUtils.isNotBlank(askReq.getAudioUuid())) {
            answerContentType = AdiConstant.ConversationConstant.ANSWER_CONTENT_TYPE_AUDIO;
        }
        return answerContentType;
    }

    /**
     * 将检索结果拼装为记忆与知识库文本。
     *
     * @param wrappers 检索结果
     * @return 记忆文本与知识文本
     */
    private Pair<String, String> buildMemoryAndKnowledge(List<RetrieverWrapper> wrappers) {
        StringBuilder memory = new StringBuilder();
        StringBuilder knowledge = new StringBuilder();
        wrappers.forEach(item -> {
            String retrieveType = item.getContentFrom();
            if (RetrieveContentFrom.CONV_MEMORY.equals(retrieveType)) {
                for (Content content : item.getResponse()) {
                    memory.append(content.textSegment().text()).append("\n");
                }
                if (memory.isEmpty()) {
                    memory.append("无\n");
                } else {
                    memory.append("\n");
                }
            } else if (RetrieveContentFrom.KNOWLEDGE_BASE.equals(retrieveType)) {
                for (Content content : item.getResponse()) {
                    knowledge.append(content.textSegment().text()).append("\n");
                }
                if (knowledge.isEmpty()) {
                    knowledge.append("无\n");
                } else {
                    knowledge.append("\n");
                }
            }
        });
        return Pair.of(memory.toString(), knowledge.toString());
    }
}
