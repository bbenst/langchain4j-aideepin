package com.moyz.adi.common.dto;

import lombok.Data;

/**
 * Session响应
 */
@Data
public class SessionResp {
    /**
     * auth
     */
    private Boolean auth;
    /**
     * 模型
     */
    private String model;
}
