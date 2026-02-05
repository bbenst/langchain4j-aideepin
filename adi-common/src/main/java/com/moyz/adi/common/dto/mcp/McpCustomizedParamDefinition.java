package com.moyz.adi.common.dto.mcp;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * MCP自定义参数Definition对象
 */
@Data
public class McpCustomizedParamDefinition {
    /**
     * 名称
     */
    private String name;
    /**
     * 标题
     */
    private String title;
    /**
     * requireEncrypt
     */
    @JsonProperty("require_encrypt")
    private Boolean requireEncrypt;
}
