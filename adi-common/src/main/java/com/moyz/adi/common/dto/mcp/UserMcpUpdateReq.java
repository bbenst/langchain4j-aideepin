package com.moyz.adi.common.dto.mcp;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * 用户MCP更新请求
 */
@Validated
@Data
public class UserMcpUpdateReq {
    /**
     * MCPID
     */
    @NotNull
    private Long mcpId;
    /**
     * MCP自定义参数
     */
    private List<UserMcpCustomizedParam> mcpCustomizedParams;
    /**
     * 是否启用
     */
    private Boolean isEnable;
}
