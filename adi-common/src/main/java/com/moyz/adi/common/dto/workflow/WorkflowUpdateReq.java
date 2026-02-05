package com.moyz.adi.common.dto.workflow;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * Workflow更新请求
 */
@Validated
@Data
public class WorkflowUpdateReq {
    /**
     * UUID
     */
    @NotBlank
    private String uuid;
    /**
     * nodes
     */
    @Size(min = 1)
    private List<WfNodeDto> nodes;
    /**
     * edges
     */
    @NotNull
    private List<WfEdgeReq> edges;
    /**
     * 删除Nodes
     */
    @NotNull
    private List<String> deleteNodes;
    /**
     * 删除Edges
     */
    @NotNull
    private List<String> deleteEdges;
}
