package com.moyz.adi.common.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 图谱边搜索对象。
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GraphEdgeSearch {
    /**
     * 源顶点搜索条件。
     */
    private GraphSearchCondition source;
    /**
     * 目标顶点搜索条件。
     */
    private GraphSearchCondition target;
    /**
     * 边搜索条件。
     */
    private GraphSearchCondition edge;
    /**
     * 返回数量限制。
     */
    @Builder.Default
    private Integer limit = 10;
    /**
     * 最大 ID（用于分页或范围控制）。
     */
    @Builder.Default
    private Long maxId = Long.MAX_VALUE;
}
