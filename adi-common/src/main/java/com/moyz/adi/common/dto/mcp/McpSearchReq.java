package com.moyz.adi.common.dto.mcp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * MCP搜索请求
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class McpSearchReq {
    /**
     * 标题
     */
    private String title;
    /**
     * 是否启用
     */
    private Boolean isEnable;
    /**
     * install类型
     */
    private String installType;
    /**
     * transport类型
     */
    private String transportType;
}
