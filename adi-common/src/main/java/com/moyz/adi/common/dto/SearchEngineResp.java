package com.moyz.adi.common.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 搜索Engine响应
 */
@Data
@Accessors(chain = true)
public class SearchEngineResp {
    /**
     * items
     */
    private List<SearchReturnWebPage> items;
}
