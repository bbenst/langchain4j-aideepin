package com.moyz.adi.common.dto;

import com.moyz.adi.common.vo.GraphEdge;
import com.moyz.adi.common.vo.GraphVertex;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Ref图谱数据传输对象
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefGraphDto {
    /**
     * entitiesFrom问题
     */
    private List<String> entitiesFromQuestion;
    /**
     * vertices
     */
    private List<GraphVertex> vertices;
    /**
     * edges
     */
    private List<GraphEdge> edges;
}
