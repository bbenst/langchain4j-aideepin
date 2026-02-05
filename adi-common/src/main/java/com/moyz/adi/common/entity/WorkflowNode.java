package com.moyz.adi.common.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.moyz.adi.common.base.JsonNodeTypeHandler;
import com.moyz.adi.common.base.NodeInputConfigTypeHandler;
import com.moyz.adi.common.workflow.WfNodeInputConfig;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.JdbcType;

import java.io.Serial;

/**
 * 工作流定义-节点 | workflow definition node
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "adi_workflow_node", autoResultMap = true)
@Schema(title = "工作流定义-节点 | workflow definition node")
public class WorkflowNode extends BaseEntity {
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
     * workflowID
     */
    @TableField("workflow_id")
    private Long workflowId;
    /**
     * workflow组件ID
     */
    @TableField("workflow_component_id")
    private Long workflowComponentId;
    /**
     * 标题
     */
    @TableField("title")
    private String title;
    /**
     * 描述
     */
    @TableField("remark")
    private String remark;
    /**
     * 输入配置
     */
    @TableField(value = "input_config", jdbcType = JdbcType.JAVA_OBJECT, typeHandler = NodeInputConfigTypeHandler.class)
    private WfNodeInputConfig inputConfig;
    /**
     * 节点配置
     */
    @TableField(value = "node_config", jdbcType = JdbcType.JAVA_OBJECT, typeHandler = JsonNodeTypeHandler.class)
    private ObjectNode nodeConfig;
    /**
     * positionX
     */
    @TableField("position_x")
    private Double positionX;
    /**
     * positionY
     */
    @TableField("position_y")
    private Double positionY;
}
