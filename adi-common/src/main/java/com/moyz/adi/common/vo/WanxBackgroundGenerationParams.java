package com.moyz.adi.common.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * WanxBackgroundGeneration参数对象
 */
@Data
public class WanxBackgroundGenerationParams {
    /**
     * 库图片URL
     */
    @JsonProperty("base_image_url")
    private String baseImageUrl;
    /**
     * ref图片URL
     */
    @JsonProperty("ref_image_url")
    private String refImageUrl;
    /**
     * ref提示词
     */
    @JsonProperty("ref_prompt")
    private String refPrompt;
}
