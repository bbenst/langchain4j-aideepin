package com.moyz.adi.common.service;

import com.moyz.adi.common.cosntant.AdiConstant;
import com.moyz.adi.common.rag.GraphStore;
import com.moyz.adi.common.vo.*;
import dev.langchain4j.store.embedding.filter.Filter;
import dev.langchain4j.store.embedding.filter.comparison.IsEqualTo;
import dev.langchain4j.store.embedding.filter.comparison.IsIn;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 知识库图谱查询服务。
 */
@Service
public class KnowledgeBaseGraphService {

    /**
     * 知识库图谱存储。
     */
    @Resource
    private GraphStore kbGraphStore;

    /**
     * 按知识库 UUID 查询图谱顶点。
     *
     * @param kbUuid 知识库 UUID
     * @param maxId  最大 ID
     * @param limit  查询数量
     * @return 顶点列表
     */
    public List<GraphVertex> listVerticesByKbUuid(String kbUuid, long maxId, int limit) {
        Filter filter = new IsEqualTo(AdiConstant.MetadataKey.KB_UUID, kbUuid);
        return kbGraphStore.searchVertices(GraphVertexSearch.builder()
                .metadataFilter(filter)
                .maxId(maxId)
                .limit(limit)
                .build());
    }

    /**
     * 按知识点 UUID 查询图谱顶点。
     *
     * @param kbItemUuid 知识点 UUID
     * @param maxId      最大 ID
     * @param limit      查询数量
     * @return 顶点列表
     */
    public List<GraphVertex> listVerticesByKbItemUuid(String kbItemUuid, long maxId, int limit) {
        Filter filter = new IsEqualTo(AdiConstant.MetadataKey.KB_ITEM_UUID, kbItemUuid);
        return kbGraphStore.searchVertices(
                GraphVertexSearch.builder()
                        .limit(limit)
                        .maxId(maxId)
                        .metadataFilter(filter)
                        .build()
        );
    }

    /**
     * 按知识库 UUID 查询图谱边。
     *
     * @param kbUuid 知识库 UUID
     * @param maxId  最大 ID
     * @param limit  查询数量
     * @return 边列表（含起止点）
     */
    public List<Triple<GraphVertex, GraphEdge, GraphVertex>> listEdgesByKbUuid(String kbUuid, long maxId, int limit) {
        Filter filter = new IsEqualTo(AdiConstant.MetadataKey.KB_UUID, kbUuid);
        return kbGraphStore.searchEdges(GraphEdgeSearch.builder()
                .edge(GraphSearchCondition.builder().metadataFilter(filter).build())
                .maxId(maxId)
                .limit(limit)
                .build());
    }

    /**
     * 按知识点 UUID 查询图谱边。
     *
     * @param kbItemUuid 知识点 UUID
     * @param maxId      最大 ID
     * @param limit      查询数量
     * @return 边列表（含起止点）
     */
    public List<Triple<GraphVertex, GraphEdge, GraphVertex>> listEdgesByKbItemUuid(String kbItemUuid, long maxId, int limit) {
        Filter filter = new IsEqualTo(AdiConstant.MetadataKey.KB_ITEM_UUID, kbItemUuid);
        return kbGraphStore.searchEdges(GraphEdgeSearch.builder()
                .edge(GraphSearchCondition.builder().metadataFilter(filter).build())
                .maxId(maxId)
                .limit(limit)
                .build());
    }

    /**
     * 从三元组结果中拆分出顶点与边列表。
     *
     * @param triples 三元组列表
     * @return 顶点列表与边列表
     */
    public Pair<List<GraphVertex>, List<GraphEdge>> getFromTriple(List<Triple<GraphVertex, GraphEdge, GraphVertex>> triples) {
        List<GraphVertex> vertices = new ArrayList<>();
        List<GraphEdge> edges = new ArrayList<>();
        for (Triple<GraphVertex, GraphEdge, GraphVertex> triple : triples) {
            vertices.add(triple.getLeft());
            vertices.add(triple.getRight());
            edges.add(triple.getMiddle());
        }
        return Pair.of(vertices, edges);
    }
}
