package com.moyz.adi.common.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 图谱边对象。
 */
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class GraphEdge {
    /**
     * 主键 ID。
     */
    private String id;
    /**
     * 标签。
     */
    private String label;
    /**
     * 权重。
     */
    private Double weight;
    /**
     * 描述。
     */
    private String description;
    /**
     * 文本段 ID。
     */
    @JsonProperty("text_segment_id")
    private String textSegmentId;
    /**
     * 元数据。
     */
    private Map<String, Object> metadata;

    /**
     * 源顶点信息。
     */
    /**
     * 起始顶点 ID。
     */
    private String startId;
    /**
     * 源名称。
     */
    private String sourceName;
    /**
     * 源元数据。
     */
    private Map<String, Object> sourceMetadata;

    /**
     * 目标顶点信息。
     */
    /**
     * 目标顶点 ID。
     */
    private String endId;
    /**
     * 目标名称。
     */
    private String targetName;
    /**
     * 目标元数据。
     */
    private Map<String, Object> targetMetadata;
}
