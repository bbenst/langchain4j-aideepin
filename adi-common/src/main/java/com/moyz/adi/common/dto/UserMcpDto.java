package com.moyz.adi.common.dto;

import com.moyz.adi.common.dto.mcp.UserMcpCustomizedParam;
import com.moyz.adi.common.entity.Mcp;
import lombok.Data;

import java.util.List;

/**
 * 用户MCP数据传输对象
 */
@Data
public class UserMcpDto {
    /**
     * 主键ID
     */
    private Long id;
    /**
     * UUID
     */
    private String uuid;
    /**
     * 用户ID
     */
    private Long userId;
    /**
     * MCPID
     */
    private Long mcpId;
    /**
     * MCP自定义参数
     */
    private List<UserMcpCustomizedParam> mcpCustomizedParams;
    /**
     * 是否启用
     */
    private Boolean isEnable;
    /**
     * MCP信息
     */
    private Mcp mcpInfo;
}
