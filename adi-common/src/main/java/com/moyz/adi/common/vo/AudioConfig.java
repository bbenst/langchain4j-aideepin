package com.moyz.adi.common.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 音频配置对象
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AudioConfig {
    /**
     * 语音
     */
    private Voice voice;

    //SampleRate、PitchRate ...

    @Data
    public static class Voice {
        /**
         * 用于API请求的音色参数名称
         */
        @JsonProperty("param_name")
        private String paramName;
        /**
         * 模型
         */
        private String model;
        /**
         * 平台
         */
        private String platform;
    }
}
