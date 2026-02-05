package com.moyz.adi.common.dto.mcp;

import lombok.Data;

import java.util.List;

/**
 * MCP列表请求
 */
@Data
public class McpListReq {
    /**
     * ids
     */
    private List<Long> ids;
}
