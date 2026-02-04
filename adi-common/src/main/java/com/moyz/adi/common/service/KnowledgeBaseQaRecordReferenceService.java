package com.moyz.adi.common.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.moyz.adi.common.dto.KbItemEmbeddingDto;
import com.moyz.adi.common.dto.RefEmbeddingDto;
import com.moyz.adi.common.entity.KnowledgeBaseQaRefEmbedding;
import com.moyz.adi.common.mapper.KnowledgeBaseQaRecordReferenceMapper;
import com.moyz.adi.common.service.embedding.IEmbeddingService;
import com.moyz.adi.common.util.EmbeddingUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * 知识库问答记录引用向量服务。
 */
@Slf4j
@Service
public class KnowledgeBaseQaRecordReferenceService extends ServiceImpl<KnowledgeBaseQaRecordReferenceMapper, KnowledgeBaseQaRefEmbedding> {

    /**
     * 向量检索服务。
     */
    @Resource
    private IEmbeddingService iEmbeddingService;

    /**
     * 根据问答记录 UUID 查询引用的向量列表。
     *
     * @param aqRecordUuid 问答记录 UUID
     * @return 引用向量列表
     */
    public List<RefEmbeddingDto> listRefEmbeddings(String aqRecordUuid) {
        List<KnowledgeBaseQaRefEmbedding> recordReferences = this.getBaseMapper().listByQaUuid(aqRecordUuid);
        if (CollectionUtils.isEmpty(recordReferences)) {
            return Collections.emptyList();
        }
        List<String> embeddingIds = recordReferences.stream().map(KnowledgeBaseQaRefEmbedding::getEmbeddingId).toList();
        if (CollectionUtils.isEmpty(embeddingIds)) {
            return Collections.emptyList();
        }
        List<KbItemEmbeddingDto> embeddings = iEmbeddingService.listByEmbeddingIds(embeddingIds);
        return EmbeddingUtil.itemToRefEmbeddingDto(embeddings);
    }
}
