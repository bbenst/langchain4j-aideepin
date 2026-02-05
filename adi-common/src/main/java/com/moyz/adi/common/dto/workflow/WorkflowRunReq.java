package com.moyz.adi.common.dto.workflow;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * WorkflowRun请求
 */
@Data
public class WorkflowRunReq {
    /**
     * inputs
     */
    private List<ObjectNode> inputs;
}
