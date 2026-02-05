package com.moyz.adi.common.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

/**
 * QA请求
 */
@Validated
@Data
public class QAReq {
    /**
     * qaRecordUUID
     */
    @NotBlank
    private String qaRecordUuid;
    /**
     * 模型名称
     */
    @NotBlank
    private String modelName;
}
