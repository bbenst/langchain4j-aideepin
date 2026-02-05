package com.moyz.adi.common.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Regenerate图片请求
 */
@Data
public class RegenerateImageReq {
    /**
     * UUID
     */
    private String uuid;
}
