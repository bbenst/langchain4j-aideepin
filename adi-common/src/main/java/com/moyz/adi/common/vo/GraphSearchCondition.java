package com.moyz.adi.common.vo;

import dev.langchain4j.store.embedding.filter.Filter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * 图谱搜索条件对象。
 */
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class GraphSearchCondition {
    /**
     * 名称列表。
     */
    protected List<String> names;
    /**
     * 元数据过滤条件。
     */
    protected Filter metadataFilter;

}
