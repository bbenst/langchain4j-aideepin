package com.moyz.adi.common.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
 * 编辑图片请求
 */
@Data
public class EditImageReq {
    /**
     * original图片
     */
    @Length(min = 32, max = 32)
    private String originalImage;
    /**
     * mask图片
     */
    @Length(min = 32, max = 32)
    private String maskImage;
    /**
     * 提示词
     */
    @NotBlank
    private String prompt;
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
