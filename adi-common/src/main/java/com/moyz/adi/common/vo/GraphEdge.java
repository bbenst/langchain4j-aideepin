package com.moyz.adi.common.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 图谱边对象
 */
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class GraphEdge {
    /**
     * 主键ID
     */
    private String id;
    /**
     * label
     */
    private String label;
    /**
     * weight
     */
    private Double weight;
    /**
     * 描述
     */
    private String description;
    /**
     * textSegmentID
     */
    @JsonProperty("text_segment_id")
    private String textSegmentId;
    /**
     * metadata
     */
    private Map<String, Object> metadata;

    //Source vertex
    /**
     * 开始ID
     */
    private String startId;
    /**
     * source名称
     */
    private String sourceName;
    /**
     * sourceMetadata
     */
    private Map<String, Object> sourceMetadata;

    //Target vertex
    /**
     * 结束ID
     */
    private String endId;
    /**
     * target名称
     */
    private String targetName;
    /**
     * targetMetadata
     */
    private Map<String, Object> targetMetadata;
}
