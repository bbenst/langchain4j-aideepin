package com.moyz.adi.common.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

/**
 * 提示词编辑请求
 */
@Data
@Validated
public class PromptEditReq {
    /**
     * 标题
     */
    @NotBlank
    private String title;
    /**
     * 描述
     */
    @NotBlank
    private String remark;
}
