package com.moyz.adi.common.rag;

import com.moyz.adi.common.vo.*;
import org.apache.commons.lang3.tuple.Triple;

import java.util.List;

/**
 * 图谱存储接口，提供顶点与边的基础读写能力。
 */
public interface GraphStore {
    /**
     * 批量新增顶点。
     *
     * @param vertexes 顶点列表
     * @return 是否新增成功
     */
    boolean addVertexes(List<GraphVertex> vertexes);

    /**
     * 新增单个顶点。
     *
     * @param vertex 顶点
     * @return 是否新增成功
     */
    boolean addVertex(GraphVertex vertex);

    /**
     * 更新顶点信息。
     *
     * @param updateInfo 更新信息
     * @return 更新后的顶点
     */
    GraphVertex updateVertex(GraphVertexUpdateInfo updateInfo);

    /**
     * 获取单个顶点。
     *
     * @param search 查询条件
     * @return 顶点信息
     */
    GraphVertex getVertex(GraphVertexSearch search);

    /**
     * 批量获取顶点。
     *
     * @param ids 顶点 ID 列表
     * @return 顶点列表
     */
    List<GraphVertex> getVertices(List<String> ids);

    /**
     * 搜索顶点。
     *
     * @param search 查询条件
     * @return 顶点列表
     */
    List<GraphVertex> searchVertices(GraphVertexSearch search);

    /**
     * 批量获取边及其两端顶点。
     *
     * @param ids 边 ID 列表
     * @return 边与顶点三元组列表
     */
    List<Triple<GraphVertex, GraphEdge, GraphVertex>> getEdges(List<String> ids);

    /**
     * 搜索边及其两端顶点。
     *
     * @param search 查询条件
     * @return 边与顶点三元组列表
     */
    List<Triple<GraphVertex, GraphEdge, GraphVertex>> searchEdges(GraphEdgeSearch search);

    /**
     * 获取单条边及其两端顶点。
     *
     * @param search 查询条件
     * @return 边与顶点三元组
     */
    Triple<GraphVertex, GraphEdge, GraphVertex> getEdge(GraphEdgeSearch search);

    /**
     * 新增边并返回完整信息。
     *
     * @param addInfo 边新增信息
     * @return 边与顶点三元组
     */
    Triple<GraphVertex, GraphEdge, GraphVertex> addEdge(GraphEdgeAddInfo addInfo);

    /**
     * 更新边信息并返回完整信息。
     *
     * @param edgeEditInfo 边编辑信息
     * @return 边与顶点三元组
     */
    Triple<GraphVertex, GraphEdge, GraphVertex> updateEdge(GraphEdgeEditInfo edgeEditInfo);

    /**
     * 删除符合条件的顶点。
     *
     * @param filter 过滤条件
     * @param includeEdges 是否同时删除相关边
     * @return 无
     */
    void deleteVertices(GraphSearchCondition filter, boolean includeEdges);

    /**
     * 删除符合条件的边。
     *
     * @param filter 过滤条件
     * @return 无
     */
    void deleteEdges(GraphSearchCondition filter);
}
