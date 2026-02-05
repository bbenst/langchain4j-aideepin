package com.moyz.adi.common.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

/**
 * QARecord请求
 */
@Validated
@Data
public class QARecordReq {
    /**
     * 问题
     */
    @NotBlank
    private String question;
    /**
     * 模型名称
     */
    private String modelName;
}
