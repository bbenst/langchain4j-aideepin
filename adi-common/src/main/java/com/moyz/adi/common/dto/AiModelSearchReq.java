package com.moyz.adi.common.dto;

import lombok.Data;

/**
 * AI模型搜索请求
 */
@Data
public class AiModelSearchReq {
    /**
     * 类型
     */
    private String type;
    /**
     * 平台
     */
    private String platform;
    /**
     * 是否启用
     */
    private Boolean isEnable;
}
