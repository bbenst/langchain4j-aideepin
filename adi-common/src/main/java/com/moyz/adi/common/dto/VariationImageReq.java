package com.moyz.adi.common.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
 * Variation图片请求
 */
@Data
public class VariationImageReq {
    /**
     * original图片
     */
    @Length(min = 32, max = 32)
    private String originalImage;
    /**
     * 数量
     */
    @NotBlank
    private String size;
    /**
     * number
     */
    @Min(1)
    @Max(10)
    private int number;
    /**
     * 模型名称
     */
    private String modelName;
}
