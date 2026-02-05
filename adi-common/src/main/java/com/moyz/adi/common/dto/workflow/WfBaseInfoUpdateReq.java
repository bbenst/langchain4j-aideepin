package com.moyz.adi.common.dto.workflow;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

/**
 * Wf基础信息更新请求
 */
@Validated
@Data
public class WfBaseInfoUpdateReq {
    /**
     * UUID
     */
    @NotBlank
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
}
