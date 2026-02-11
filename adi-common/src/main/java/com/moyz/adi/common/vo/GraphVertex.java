package com.moyz.adi.common.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 图谱顶点对象。
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GraphVertex {
    /**
     * 主键 ID。
     */
    private String id;
    /**
     * Apache AGE 暂不支持多标签。
     */
    /**
     * 标签。
     */
    private String label;
    /**
     * 名称。
     */
    private String name;
    /**
     * 对应的文本段 ID。
     */
    @JsonProperty("text_segment_id")
    private String textSegmentId;
    /**
     * 描述。
     */
    private String description;
    /**
     * 元数据。
     */
    private Map<String, Object> metadata;
}
