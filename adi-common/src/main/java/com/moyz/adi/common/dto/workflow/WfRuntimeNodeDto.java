package com.moyz.adi.common.dto.workflow;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

/**
 * Wf运行时节点数据传输对象
 */
@Validated
@Data
public class WfRuntimeNodeDto {
    /**
     * 主键ID
     */
    private Long id;
    /**
     * UUID
     */
    private String uuid;
    /**
     * workflow运行时ID
     */
    private Long workflowRuntimeId;
    /**
     * 节点ID
     */
    private Long nodeId;
    /**
     * 输入
     */
    private ObjectNode input;
    /**
     * 输出
     */
    private ObjectNode output;
    /**
     * 状态
     */
    private Integer status;
}
