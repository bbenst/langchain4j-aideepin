package com.moyz.adi.common.vo;

import dev.langchain4j.store.embedding.filter.Filter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 图谱顶点更新信息对象。
 */
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class GraphVertexUpdateInfo {
    /**
     * 新的顶点数据。
     */
    private GraphVertex newData;

    /**
     * 过滤条件。
     */
    /**
     * 顶点名称。
     */
    private String name;
    /**
     * 元数据过滤条件。
     */
    private Filter metadataFilter;
}
