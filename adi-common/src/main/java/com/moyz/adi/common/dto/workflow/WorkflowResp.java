package com.moyz.adi.common.dto.workflow;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Workflow响应
 */
@Data
public class WorkflowResp {
    /**
     * 主键ID
     */
    private Long id;
    /**
     * UUID
     */
    private String uuid;
    /**
     * 标题
     */
    private String title;
    /**
     * 描述
     */
    private String remark;
    /**
     * 是否公开
     */
    private Boolean isPublic;
    /**
     * 用户ID
     */
    private Long userId;
    /**
     * 用户UUID
     */
    private String userUuid;
    /**
     * 用户名称
     */
    private String userName;
    /**
     * nodes
     */
    private List<WfNodeDto> nodes;
    /**
     * edges
     */
    private List<WfEdgeReq> edges;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
