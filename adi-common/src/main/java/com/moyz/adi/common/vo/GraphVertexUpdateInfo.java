package com.moyz.adi.common.vo;

import dev.langchain4j.store.embedding.filter.Filter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 图谱Vertex更新信息对象
 */
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class GraphVertexUpdateInfo {
    /**
     * newData
     */
    private GraphVertex newData;

    //Filter
    /**
     * 名称
     */
    private String name;
    /**
     * metadataFilter
     */
    private Filter metadataFilter;
}
