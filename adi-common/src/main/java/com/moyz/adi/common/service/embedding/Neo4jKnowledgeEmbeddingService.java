package com.moyz.adi.common.service.embedding;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moyz.adi.common.cosntant.AdiConstant;
import com.moyz.adi.common.dto.KbItemEmbeddingDto;
import com.moyz.adi.common.rag.neo4j.AdiNeo4jEmbeddingStore;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.filter.comparison.IsEqualTo;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


/**
 * 知识库向量存储服务（Neo4j）。
 */
@Slf4j
@Service
@ConditionalOnProperty(value = "adi.vector-database", havingValue = "neo4j")
public class Neo4jKnowledgeEmbeddingService implements IEmbeddingService {

    /**
     * 向量存储。
     */
    @Resource
    private EmbeddingStore<TextSegment> embeddingStore;

    /**
     * 根据向量 ID 列表查询向量信息。
     *
     * @param embeddingIds 向量 ID 列表
     * @return 向量 DTO 列表
     */
    @Override
    public List<KbItemEmbeddingDto> listByEmbeddingIds(List<String> embeddingIds) {
        if (embeddingIds.isEmpty()) {
            log.warn("listByEmbeddingIds embeddingIds is empty");
            return new ArrayList<>();
        }
        EmbeddingSearchResult<TextSegment> searchResult = ((AdiNeo4jEmbeddingStore) embeddingStore).searchByIds(embeddingIds);
        List<KbItemEmbeddingDto> result = new ArrayList<>();
        for (EmbeddingMatch<TextSegment> embeddingMatch : searchResult.matches()) {
            result.add(
                    KbItemEmbeddingDto
                            .builder()
                            .embeddingId(embeddingMatch.embeddingId())
                            .embedding(embeddingMatch.embedding().vector())
                            .text(embeddingMatch.embedded().text())
                            .build()
            );
        }
        return result;
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
        EmbeddingSearchResult<TextSegment> searchResult = ((AdiNeo4jEmbeddingStore) embeddingStore).searchByMetadata(new IsEqualTo(AdiConstant.MetadataKey.KB_ITEM_UUID, kbItemUuid), 1000);

        List<EmbeddingMatch<TextSegment>> ss = searchResult.matches();

        int total = ss.size();
        int fromIndex = (currentPage - 1) * pageSize;
        if (total <= fromIndex) {
            Page<KbItemEmbeddingDto> result = new Page<>();
            result.setRecords(new ArrayList<>());
            result.setTotal(total);
            result.setPages((int) (double) (total / pageSize));
            result.setSize(pageSize);
            result.setCurrent(currentPage);
            return result;
        }
        int endIndex = Math.min(total, fromIndex + pageSize);
        List<EmbeddingMatch<TextSegment>> pageSearchResult;
        if (total <= pageSize) {
            pageSearchResult = ss;
        } else {
            pageSearchResult = ss.subList(fromIndex, endIndex);
        }
        List<KbItemEmbeddingDto> records = new ArrayList<>();
        for (EmbeddingMatch<TextSegment> embeddingMatch : pageSearchResult) {
            records.add(
                    KbItemEmbeddingDto
                            .builder()
                            .embeddingId(embeddingMatch.embeddingId())
                            .embedding(embeddingMatch.embedding().vector())
                            .text(embeddingMatch.embedded().text())
                            .build()
            );
        }
        Page<KbItemEmbeddingDto> result = new Page<>();
        result.setRecords(records);
        result.setTotal(total);
        result.setPages((int) (double) (total / pageSize));
        result.setSize(pageSize);
        result.setCurrent(currentPage);
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
        embeddingStore.removeAll(new IsEqualTo(AdiConstant.MetadataKey.KB_ITEM_UUID, kbItemUuid));
        return true;
    }

    /**
     * 统计知识库下的向量数量。
     *
     * @param kbUuid 知识库 UUID
     * @return 数量
     */
    @Override
    public Integer countByKbUuid(String kbUuid) {
        return ((AdiNeo4jEmbeddingStore) embeddingStore).countByMetadata(new IsEqualTo(AdiConstant.MetadataKey.KB_UUID, kbUuid));
    }
}
