package com.moyz.adi.common.dto.workflow;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

/**
 * Wf组件请求
 */
@Data
@Validated
public class WfComponentReq {
    /**
     * UUID
     */
    private String uuid;
    /**
     * 名称
     */
    @NotBlank(message = "标题不能为空")
    private String name;
    /**
     * 标题
     */
    @NotBlank(message = "标题不能为空")
    private String title;
    /**
     * 描述
     */
    private String remark;
    /**
     * 是否启用
     */
    private Boolean isEnable;
    /**
     * display排序
     */
    private Integer displayOrder;
}
