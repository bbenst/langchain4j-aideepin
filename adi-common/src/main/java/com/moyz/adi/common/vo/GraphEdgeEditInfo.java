package com.moyz.adi.common.vo;

import lombok.Data;

/**
 * 图谱边编辑信息对象
 */
@Data
public class GraphEdgeEditInfo {
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
