package com.moyz.adi.common.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.moyz.adi.common.interfaces.AbstractSearchEngineService;
import lombok.Data;

/**
 * 搜索Engine信息对象
 */
@Data
public class SearchEngineInfo {
    /**
     * 名称
     */
    private String name;
    /**
     * 启用
     */
    private Boolean enable;
    /**
     * 搜索EngineService
     */
    @JsonIgnore
    private AbstractSearchEngineService searchEngineService;
}
