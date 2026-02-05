package com.moyz.adi.common.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * 图谱Vertex搜索对象
 */
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GraphVertexSearch extends GraphSearchCondition {
    /**
     * label
     */
    private String label;
    /**
     * textSegmentID
     */
    @JsonProperty("text_segment_id")
    private String textSegmentId;
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
