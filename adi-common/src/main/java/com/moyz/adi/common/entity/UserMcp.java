package com.moyz.adi.common.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.moyz.adi.common.config.UserMcpSettingTypeHandler;
import com.moyz.adi.common.dto.mcp.UserMcpCustomizedParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.JdbcType;

import java.util.List;

/**
 * 用户MCP实体
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName(value = "adi_user_mcp", autoResultMap = true)
@Schema(title = "用户MCP实体")
public class UserMcp extends BaseEntity {
    /**
     * UUID
     */
    @Schema(title = "uuid")
    @TableField(value = "uuid")
    private String uuid;
    /**
     * 用户ID
     */
    @Schema(title = "user_id")
    @TableField(value = "user_id")
    private Long userId;
    /**
     * MCPID
     */
    @Schema(title = "mcp_id")
    @TableField(value = "mcp_id")
    private Long mcpId;
    /**
     * 用户的参数设置
     */
    @Schema(title = "用户的参数设置")
    @TableField(value = "mcp_customized_params", jdbcType = JdbcType.ARRAY, typeHandler = UserMcpSettingTypeHandler.class)
    private List<UserMcpCustomizedParam> mcpCustomizedParams;
    /**
     * 是否启用
     */
    @Schema(title = "是否启用")
    @TableField(value = "is_enable")
    private Boolean isEnable;
}
