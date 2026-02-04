package com.moyz.adi.common.service.embedding;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.moyz.adi.common.dto.KbItemEmbeddingDto;
import com.moyz.adi.common.entity.KnowledgeBaseEmbedding;
import com.moyz.adi.common.mapper.KnowledgeBaseEmbeddingMapper;
import com.moyz.adi.common.util.AdiPropertiesUtil;
import com.moyz.adi.common.util.MPPageUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * 知识库向量存储服务（PGVector）。
 */
@Slf4j
@Service
@ConditionalOnProperty(value = "adi.vector-database", havingValue = "pgvector")
public class KnowledgeBaseEmbeddingService extends ServiceImpl<KnowledgeBaseEmbeddingMapper, KnowledgeBaseEmbedding> implements IEmbeddingService {

    /**
     * 根据向量 ID 列表查询向量内容。
     *
     * @param embeddingIds 向量 ID 列表
     * @return 向量 DTO 列表
     */
    @Override
    public List<KbItemEmbeddingDto> listByEmbeddingIds(List<String> embeddingIds) {
        LambdaQueryWrapper<KnowledgeBaseEmbedding> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(KnowledgeBaseEmbedding::getEmbeddingId, embeddingIds.stream().map(UUID::fromString).toList());
        List<KnowledgeBaseEmbedding> embeddingList = baseMapper.selectList(lambdaQueryWrapper);
        return MPPageUtil.convertToList(embeddingList, KbItemEmbeddingDto.class, (s, t) -> {
            t.setEmbedding(s.getEmbedding().toArray());
            return t;
        });
    }

    /**
     * 根据知识点 UUID 分页查询向量。
     *
     * @param kbItemUuid  知识点 UUID
     * @param currentPage 当前页
     * @param pageSize    页大小
     * @return 分页结果
     */
    @Override
    public Page<KbItemEmbeddingDto> listByItemUuid(String kbItemUuid, int currentPage, int pageSize) {
        Page<KnowledgeBaseEmbedding> sourcePage = baseMapper.selectByItemUuid(new Page<>(currentPage, pageSize), kbItemUuid, AdiPropertiesUtil.EMBEDDING_TABLE_SUFFIX);
        Page<KbItemEmbeddingDto> result = new Page<>();
        MPPageUtil.convertToPage(sourcePage, result, KbItemEmbeddingDto.class, (source, target) -> {
            target.setEmbedding(source.getEmbedding().toArray());
            return target;
        });
        return result;
    }

    /**
     * 删除指定知识点的向量。
     *
     * @param kbItemUuid 知识点 UUID
     * @return 是否删除成功
     */
    @Override
    public boolean deleteByItemUuid(String kbItemUuid) {
        return baseMapper.deleteByItemUuid(kbItemUuid, AdiPropertiesUtil.EMBEDDING_TABLE_SUFFIX);
    }

    /**
     * 统计知识库下的向量数量。
     *
     * @param kbUuid 知识库 UUID
     * @return 数量
     */
    @Override
    public Integer countByKbUuid(String kbUuid) {
        return baseMapper.countByKbUuid(kbUuid, AdiPropertiesUtil.EMBEDDING_TABLE_SUFFIX);
    }
}
