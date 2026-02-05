package com.moyz.adi.common.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 图谱边Get对象
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GraphEdgeGet {
    /**
     * source
     */
    private String source;
    /**
     * target
     */
    private String target;
    /**
     * sourceMetadata
     */
    private Map<String, Object> sourceMetadata;
    /**
     * targetMetadata
     */
    private Map<String, Object> targetMetadata;

}
