package com.moyz.adi.common.vo;

import lombok.Data;

/**
 * 图谱边编辑信息对象。
 */
@Data
public class GraphEdgeEditInfo {
    /**
     * 边数据。
     */
    private GraphEdge edge;
    /**
     * 源顶点过滤条件。
     */
    private GraphSearchCondition sourceFilter;
    /**
     * 目标顶点过滤条件。
     */
    private GraphSearchCondition targetFilter;
}
