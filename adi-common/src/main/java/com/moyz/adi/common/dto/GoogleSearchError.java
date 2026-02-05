package com.moyz.adi.common.dto;

import lombok.Data;

/**
 * Google搜索Error对象
 */
@Data
public class GoogleSearchError {
    /**
     * code
     */
    private Integer code;
    /**
     * 消息
     */
    private String message;
}
