package com.moyz.adi.common.dto;

import lombok.Data;

import java.util.List;

/**
 * 搜索Return对象
 */
@Data
public class SearchReturn {
    /**
     * error消息
     */
    private String errorMessage;
    /**
     * items
     */
    private List<SearchReturnWebPage> items;
}
