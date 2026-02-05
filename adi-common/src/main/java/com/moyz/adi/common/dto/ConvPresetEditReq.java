package com.moyz.adi.common.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

/**
 * Conv预设编辑请求
 */
@Data
@Validated
public class ConvPresetEditReq {
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
    /**
     * AI系统消息
     */
    @NotBlank
    private String aiSystemMessage;
}
