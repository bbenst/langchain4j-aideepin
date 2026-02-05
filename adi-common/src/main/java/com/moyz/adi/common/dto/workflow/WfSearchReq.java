package com.moyz.adi.common.dto.workflow;

import lombok.Data;

/**
 * Wf搜索请求
 */
@Data
public class WfSearchReq {
    /**
     * 标题
     */
    private String title;
    /**
     * 是否启用
     */
    private Boolean isEnable;
    /**
     * 是否公开
     */
    private Boolean isPublic;
}
