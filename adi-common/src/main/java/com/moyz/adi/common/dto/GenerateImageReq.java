package com.moyz.adi.common.dto;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Generate图片请求
 */
@Data
public class GenerateImageReq {
    /**
     * 提示词
     */
    @NotBlank
    private String prompt;
    /**
     * negative提示词
     */
    private String negativePrompt;
    /**
     * 数量
     */
    private String size;
    /**
     * quality
     */
    private String quality;
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
    /**
     * seed
     */
    private int seed;
    /**
     * dynamic参数
     */
    private JsonNode dynamicParams;
    /**
     * interactingMethod
     */
    private int interactingMethod;
}
