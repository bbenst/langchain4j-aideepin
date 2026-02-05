package com.moyz.adi.common.dto;

import lombok.Data;

/**
 * Login响应
 */
@Data
public class LoginResp {
    /**
     * Token
     */
    private String token;
    /**
     * 名称
     */
    private String name;
    /**
     * 邮箱
     */
    private String email;
    /**
     * 激活时间
     */
    private String activeTime;
    /**
     * captchaID
     */
    private String captchaId;
    /**
     * UUID
     */
    private String uuid;
}
