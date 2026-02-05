package com.moyz.adi.common.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Tts配置对象
 */
@Data
public class TtsSetting {
    /**
     * synthesizerSide
     */
    @JsonProperty("synthesizer_side")
    private String synthesizerSide;
    /**
     * 模型名称
     */
    @JsonProperty("model_name")
    private String modelName;
    /**
     * 平台
     */
    private String platform;
}
