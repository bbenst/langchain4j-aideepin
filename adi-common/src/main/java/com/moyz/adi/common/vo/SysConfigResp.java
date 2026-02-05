package com.moyz.adi.common.vo;

import com.moyz.adi.common.languagemodel.data.ModelVoice;
import lombok.Data;

import java.util.List;

/**
 * Sys配置响应
 */
@Data
public class SysConfigResp {
    /**
     * asr配置
     */
    private AsrSetting asrSetting;
    /**
     * tts配置
     */
    private TtsSetting ttsSetting;
    /**
     * availableVoices
     */
    private List<ModelVoice> availableVoices;
}
