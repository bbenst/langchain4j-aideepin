package com.moyz.adi.common.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 工作流定义-边 | workflow definition edge
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("adi_workflow_edge")
@Schema(title = "工作流定义-边 | workflow definition edge")
public class WorkflowEdge extends BaseEntity {
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
     * source节点UUID
     */
    @TableField("source_node_uuid")
    private String sourceNodeUuid;
    /**
     * sourceHandle
     */
    @TableField("source_handle")
    private String sourceHandle;
    /**
     * target节点UUID
     */
    @TableField("target_node_uuid")
    private String targetNodeUuid;
}
