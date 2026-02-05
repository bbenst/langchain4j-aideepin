package com.moyz.adi.common.dto.workflow;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Wf运行时响应
 */
@Data
public class WfRuntimeResp {
    /**
     * 主键ID
     */
    private Long id;
    /**
     * UUID
     */
    private String uuid;
    /**
     * workflowID
     */
    private Long workflowId;
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
    /**
     * 状态描述
     */
    private String statusRemark;
    /**
     * workflowUUID
     */
    private String workflowUuid;
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
