package com.moyz.adi.common.dto;

import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * Prompts保存请求
 */
@Data
@Validated
public class PromptsSaveReq {
    /**
     * prompts
     */
    @Length(min = 1)
    private List<PromptDto> prompts;
}
