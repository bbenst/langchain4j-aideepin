package com.moyz.adi.common.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 图谱Vertex对象
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GraphVertex {
    /**
     * 主键ID
     */
    private String id;
    //Apache AGE暂时不支持多标签
    /**
     * label
     */
    private String label;
    /**
     * 名称
     */
    private String name;
    /**
     * 如对应的文本段id
     */
    @JsonProperty("text_segment_id")
    private String textSegmentId;
    /**
     * 描述
     */
    private String description;
    /**
     * metadata
     */
    private Map<String, Object> metadata;
}
