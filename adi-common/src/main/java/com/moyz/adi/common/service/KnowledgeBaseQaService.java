package com.moyz.adi.common.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.ChainWrappers;
import com.moyz.adi.common.base.ThreadContext;
import com.moyz.adi.common.dto.KbQaDto;
import com.moyz.adi.common.dto.QARecordReq;
import com.moyz.adi.common.dto.RefGraphDto;
import com.moyz.adi.common.entity.*;
import com.moyz.adi.common.exception.BaseException;
import com.moyz.adi.common.mapper.KnowledgeBaseQaRecordMapper;
import com.moyz.adi.common.util.JsonUtil;
import com.moyz.adi.common.util.MPPageUtil;
import com.moyz.adi.common.util.UuidUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

import static com.moyz.adi.common.enums.ErrorEnum.A_DATA_NOT_FOUND;
import static com.moyz.adi.common.util.LocalCache.MODEL_ID_TO_OBJ;

/**
 * 知识库问答记录服务。
 */
@Slf4j
@Service
public class KnowledgeBaseQaService extends ServiceImpl<KnowledgeBaseQaRecordMapper, KnowledgeBaseQa> {

    /**
     * 向量引用记录服务。
     */
    @Resource
    private KnowledgeBaseQaRecordReferenceService knowledgeBaseQaRecordReferenceService;

    /**
     * 图谱引用记录服务。
     */
    @Resource
    private KnowledgeBaseQaRefGraphService knowledgeBaseQaRecordRefGraphService;

    /**
     * 模型服务。
     */
    @Resource
    private AiModelService aiModelService;

    /**
     * 新增问答记录。
     *
     * @param knowledgeBase 知识库
     * @param req           记录请求
     * @return 记录 DTO
     */
    public KbQaDto add(KnowledgeBase knowledgeBase, QARecordReq req) {
        KnowledgeBaseQa newRecord = new KnowledgeBaseQa();
        newRecord.setAiModelId(aiModelService.getIdByName(req.getModelName()));
        newRecord.setQuestion(req.getQuestion());
        newRecord.setKbId(knowledgeBase.getId());
        newRecord.setKbUuid((knowledgeBase.getUuid()));
        newRecord.setUuid(UuidUtil.createShort());
        newRecord.setUserId(ThreadContext.getCurrentUserId());
        baseMapper.insert(newRecord);

        KbQaDto result = new KbQaDto();
        BeanUtils.copyProperties(newRecord, result);
        return result;
    }

    /**
     * 分页查询问答记录。
     *
     * @param kbUuid      知识库 UUID
     * @param keyword     关键词
     * @param currentPage 当前页
     * @param pageSize    页大小
     * @return 分页结果
     */
    public Page<KbQaDto> search(String kbUuid, String keyword, Integer currentPage, Integer pageSize) {
        LambdaQueryWrapper<KnowledgeBaseQa> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KnowledgeBaseQa::getKbUuid, kbUuid);
        wrapper.eq(KnowledgeBaseQa::getIsDeleted, false);
        if (Boolean.FALSE.equals(ThreadContext.getCurrentUser().getIsAdmin())) {
            wrapper.eq(KnowledgeBaseQa::getUserId, ThreadContext.getCurrentUserId());
        }
        if (StringUtils.isNotBlank(keyword)) {
            wrapper.like(KnowledgeBaseQa::getQuestion, keyword);
        }
        wrapper.orderByDesc(KnowledgeBaseQa::getUpdateTime);
        Page<KnowledgeBaseQa> page = baseMapper.selectPage(new Page<>(currentPage, pageSize), wrapper);

        Page<KbQaDto> result = new Page<>();
        MPPageUtil.convertToPage(page, result, KbQaDto.class, (t1, t2) -> {
            AiModel aiModel = MODEL_ID_TO_OBJ.get(t1.getAiModelId());
            t2.setAiModelPlatform(null == aiModel ? "" : aiModel.getPlatform());
            return t2;
        });
        return result;
    }

    /**
     * 增加嵌入引用记录。
     *
     * @param user             用户
     * @param qaRecordId       记录 ID
     * @param embeddingToScore 向量与分数映射
     * @return 无
     */
    public void createEmbeddingRefs(User user, Long qaRecordId, Map<String, Double> embeddingToScore) {
        log.info("更新向量引用,userId:{},qaRecordId:{},embeddingToScore.size:{}", user.getId(), qaRecordId, embeddingToScore.size());
        // 逐条持久化引用分数，便于后续溯源与排序
        for (Map.Entry<String, Double> entry : embeddingToScore.entrySet()) {
            String embeddingId = entry.getKey();
            KnowledgeBaseQaRefEmbedding recordReference = new KnowledgeBaseQaRefEmbedding();
            recordReference.setQaRecordId(qaRecordId);
            recordReference.setEmbeddingId(embeddingId);
            recordReference.setScore(embeddingToScore.get(embeddingId));
            recordReference.setUserId(user.getId());
            knowledgeBaseQaRecordReferenceService.save(recordReference);
        }
    }

    /**
     * 增加图谱引用记录。
     *
     * @param user      用户
     * @param qaRecordId 记录 ID
     * @param graphDto  图谱引用
     * @return 无
     */
    public void createGraphRefs(User user, Long qaRecordId, RefGraphDto graphDto) {
        log.info("更新图谱引用,userId:{},qaRecordId:{},vertices.Size:{},edges.size:{}", user.getId(), qaRecordId, graphDto.getVertices().size(), graphDto.getEdges().size());
        // 将问题中抽取的实体拼接存储，便于检索时快速展示
        String entities = null == graphDto.getEntitiesFromQuestion() ? "" : String.join(",", graphDto.getEntitiesFromQuestion());
        // 以原始结构保留图谱信息，避免结构化字段丢失细节
        Map<String, Object> graphFromStore = new HashMap<>();
        graphFromStore.put("vertices", graphDto.getVertices());
        graphFromStore.put("edges", graphDto.getEdges());
        KnowledgeBaseQaRefGraph refGraph = new KnowledgeBaseQaRefGraph();
        refGraph.setQaRecordId(qaRecordId);
        refGraph.setUserId(user.getId());
        refGraph.setEntitiesFromQuestion(entities);
        refGraph.setGraphFromStore(JsonUtil.toJson(graphFromStore));
        knowledgeBaseQaRecordRefGraphService.save(refGraph);
    }

    /**
     * 获取问答记录，不存在则抛异常。
     *
     * @param uuid 记录 UUID
     * @return 记录实体
     * @throws BaseException 记录不存在时抛出异常
     */
    public KnowledgeBaseQa getOrThrow(String uuid) {
        // 仅查询未删除记录，避免逻辑删除数据被再次使用
        KnowledgeBaseQa exist = ChainWrappers.lambdaQueryChain(baseMapper)
                .eq(KnowledgeBaseQa::getUuid, uuid)
                .eq(KnowledgeBaseQa::getIsDeleted, false)
                .one();
        // 未找到记录直接抛出，保持调用方处理一致
        if (null == exist) {
            throw new BaseException(A_DATA_NOT_FOUND);
        }
        return exist;
    }

    /**
     * 清理当前用户的问答记录。
     */
    public void clearByCurrentUser() {
        ChainWrappers.lambdaUpdateChain(baseMapper)
                .eq(KnowledgeBaseQa::getUserId, ThreadContext.getCurrentUserId())
                .set(KnowledgeBaseQa::getIsDeleted, true)
                .update();
    }

    /**
     * 软删除问答记录。
     *
     * @param uuid 记录 UUID
     * @return 是否删除成功
     */
    public boolean softDelete(String uuid) {
        if (Boolean.TRUE.equals(ThreadContext.getCurrentUser().getIsAdmin())) {
            return ChainWrappers.lambdaUpdateChain(baseMapper)
                    .eq(KnowledgeBaseQa::getUuid, uuid)
                    .set(KnowledgeBaseQa::getIsDeleted, true)
                    .update();
        }
        KnowledgeBaseQa exist = ChainWrappers.lambdaQueryChain(baseMapper)
                .eq(KnowledgeBaseQa::getUuid, uuid)
                .one();
        if (null == exist) {
            throw new BaseException(A_DATA_NOT_FOUND);
        }
        return ChainWrappers.lambdaUpdateChain(baseMapper)
                .eq(KnowledgeBaseQa::getId, exist.getId())
                .set(KnowledgeBaseQa::getIsDeleted, true)
                .update();
    }
}
