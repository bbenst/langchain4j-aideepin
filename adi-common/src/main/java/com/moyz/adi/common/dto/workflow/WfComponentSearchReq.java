package com.moyz.adi.common.dto.workflow;

import lombok.Data;

/**
 * Wf组件搜索请求
 */
@Data
public class WfComponentSearchReq {
    /**
     * 标题
     */
    private String title;
    /**
     * 是否启用
     */
    private Boolean isEnable;
}
