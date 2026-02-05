package com.moyz.adi.common.dto.workflow;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

/**
 * Wf新增请求
 */
@Data
@Validated
public class WfAddReq {
    /**
     * 标题
     */
    @NotBlank
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
