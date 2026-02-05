package com.moyz.adi.common.dto.workflow;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

/**
 * Wf边请求
 */
@Validated
@Data
public class WfEdgeReq {
    /**
     * 主键ID
     */
    private Long id;
    /**
     * UUID
     */
    @NotBlank
    private String uuid;
    /**
     * workflowID
     */
    @Min(1)
    private Long workflowId;
    /**
     * source节点UUID
     */
    @NotBlank
    private String sourceNodeUuid;
    /**
     * sourceHandle
     */
    private String sourceHandle;
    /**
     * target节点UUID
     */
    @NotBlank
    private String targetNodeUuid;
    /**
     * 是否新增
     */
    private Boolean isNew;
}
