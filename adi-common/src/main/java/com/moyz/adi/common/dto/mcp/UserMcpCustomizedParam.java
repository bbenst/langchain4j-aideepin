package com.moyz.adi.common.dto.mcp;

import lombok.Data;

/**
 * 用户设置的MCP参数
 */
@Data
public class UserMcpCustomizedParam {
    /**
     * 名称
     */
    private String name;
    /**
     * 值
     */
    private Object value;
    /**
     * encrypted
     */
    private Boolean encrypted;
}
