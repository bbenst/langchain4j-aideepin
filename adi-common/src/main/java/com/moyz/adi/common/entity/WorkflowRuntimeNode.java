package com.moyz.adi.common.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.moyz.adi.common.base.JsonNodeTypeHandler;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.JdbcType;

import java.io.Serial;

/**
 * 工作流实例-节点 | Workflow runtime - node
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "adi_workflow_runtime_node", autoResultMap = true)
@Schema(title = "工作流实例-节点 | Workflow runtime - node")
public class WorkflowRuntimeNode extends BaseEntity {
    /**
     * 序列化版本号
     */
    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * UUID
     */
    @TableField("uuid")
    private String uuid;
    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;
    /**
     * workflow运行时ID
     */
    @TableField("workflow_runtime_id")
    private Long workflowRuntimeId;
    /**
     * 节点ID
     */
    @TableField("node_id")
    private Long nodeId;
    /**
     * 输入
     */
    @TableField(value = "input", jdbcType = JdbcType.JAVA_OBJECT, typeHandler = JsonNodeTypeHandler.class)
    private ObjectNode input;
    /**
     * 输出
     */
    @TableField(value = "\"output\"", jdbcType = JdbcType.JAVA_OBJECT, typeHandler = JsonNodeTypeHandler.class)
    private ObjectNode output;
    /**
     * 状态
     */
    @TableField("status")
    private Integer status;
    /**
     * 状态描述
     */
    @TableField("status_remark")
    private String statusRemark;
}
