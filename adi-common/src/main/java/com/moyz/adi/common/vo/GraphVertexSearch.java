package com.moyz.adi.common.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * 图谱顶点搜索对象。
 */
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GraphVertexSearch extends GraphSearchCondition {
    /**
     * 标签。
     */
    private String label;
    /**
     * 文本段 ID。
     */
    @JsonProperty("text_segment_id")
    private String textSegmentId;
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
