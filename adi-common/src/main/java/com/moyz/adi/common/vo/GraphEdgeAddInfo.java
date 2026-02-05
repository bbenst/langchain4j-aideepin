package com.moyz.adi.common.vo;

import lombok.Data;

/**
 * 图谱边新增信息对象
 */
@Data
public class GraphEdgeAddInfo {
    /**
     * 边
     */
    private GraphEdge edge;
    /**
     * sourceFilter
     */
    private GraphSearchCondition sourceFilter;
    /**
     * targetFilter
     */
    private GraphSearchCondition targetFilter;
}
