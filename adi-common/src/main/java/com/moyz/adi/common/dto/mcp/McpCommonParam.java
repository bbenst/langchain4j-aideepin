package com.moyz.adi.common.dto.mcp;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 已由系统管理员初始化完成的参数
 */
@Data
public class McpCommonParam {
    /**
     * 名称
     */
    private String name;
    /**
     * 标题
     */
    private String title;
    /**
     * 值
     */
    private Object value;
    /**
     * requireEncrypt
     */
    @JsonProperty("require_encrypt")
    private Boolean requireEncrypt;
    /**
     * encrypted
     */
    private Boolean encrypted;
}
