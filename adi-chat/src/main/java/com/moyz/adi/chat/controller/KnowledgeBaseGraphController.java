package com.moyz.adi.chat.controller;

import com.moyz.adi.common.service.KnowledgeBaseGraphService;
import com.moyz.adi.common.vo.GraphEdge;
import com.moyz.adi.common.vo.GraphVertex;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
/**
 * 知识图谱查询接口控制器。
 */
@RestController
@RequestMapping("/knowledge-base-graph")
@Validated
public class KnowledgeBaseGraphController {
    /**
     * 知识图谱服务，负责顶点与边的查询。
     */
    @Resource
    private KnowledgeBaseGraphService knowledgeBaseGraphService;

    /**
     * 获取知识点关联的图谱顶点与边。
     *
     * @param kbItemUuid 知识点 UUID
     * @param maxVertexId 顶点游标 ID
     * @param maxEdgeId 边游标 ID
     * @param limit 返回数量限制
     * @return 包含顶点与边的结果
     */
    @GetMapping("/list/{kbItemUuid}")
    public Map<String, Object> list(@PathVariable String kbItemUuid, @RequestParam(defaultValue = Long.MAX_VALUE + "") Long maxVertexId, @RequestParam(defaultValue = Long.MAX_VALUE + "") Long maxEdgeId, @RequestParam(defaultValue = "-1") int limit) {
        List<GraphVertex> vertices = knowledgeBaseGraphService.listVerticesByKbItemUuid(kbItemUuid, maxVertexId, limit);
        List<Triple<GraphVertex, GraphEdge, GraphVertex>> edgeWithVertices = knowledgeBaseGraphService.listEdgesByKbItemUuid(kbItemUuid, maxEdgeId, limit);
        Pair<List<GraphVertex>, List<GraphEdge>> pair = knowledgeBaseGraphService.getFromTriple(edgeWithVertices);
        vertices.addAll(pair.getLeft());
        // 顶点与边返回可能存在重复顶点，需要按 ID 去重避免前端渲染异常
        List<GraphVertex> filteredVertices = vertices
                .stream()
                .collect(
                        Collectors.toMap(
                                GraphVertex::getId, Function.identity(), (s, a) -> s
                        )
                )
                .values()
                .stream()
                .toList();
        return Map.of("vertices", filteredVertices, "edges", pair.getRight());
    }
}
