package com.moyz.adi.common.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 图谱边查询参数对象。
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GraphEdgeGet {
    /**
     * 源名称。
     */
    private String source;
    /**
     * 目标名称。
     */
    private String target;
    /**
     * 源元数据。
     */
    private Map<String, Object> sourceMetadata;
    /**
     * 目标元数据。
     */
    private Map<String, Object> targetMetadata;

}
