package com.moyz.adi.common.vo;

import dev.langchain4j.store.embedding.filter.Filter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * 图谱搜索Condition对象
 */
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class GraphSearchCondition {
    /**
     * names
     */
    protected List<String> names;
    /**
     * metadataFilter
     */
    protected Filter metadataFilter;

}
