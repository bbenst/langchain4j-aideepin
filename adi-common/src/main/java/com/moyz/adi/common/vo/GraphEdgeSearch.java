package com.moyz.adi.common.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 图谱边搜索对象
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GraphEdgeSearch {
    /**
     * source
     */
    private GraphSearchCondition source;
    /**
     * target
     */
    private GraphSearchCondition target;
    /**
     * 边
     */
    private GraphSearchCondition edge;
    /**
     * 限制
     */
    @Builder.Default
    private Integer limit = 10;
    /**
     * 最大ID
     */
    @Builder.Default
    private Long maxId = Long.MAX_VALUE;
}
