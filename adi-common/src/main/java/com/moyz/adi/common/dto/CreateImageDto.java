package com.moyz.adi.common.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

/**
 * Create图片数据传输对象
 */
@Data
public class CreateImageDto {
    /**
     * 提示词
     */
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
    private int number;
    /**
     * interactingMethod
     */
    private int interactingMethod;
    /**
     * seed
     */
    private int seed;
    /**
     * original图片
     */
    private String originalImage;
    /**
     * mask图片
     */
    private String maskImage;
    /**
     * 模型名称
     */
    private String modelName;
    /**
     * dynamic参数
     */
    private JsonNode dynamicParams;
}
