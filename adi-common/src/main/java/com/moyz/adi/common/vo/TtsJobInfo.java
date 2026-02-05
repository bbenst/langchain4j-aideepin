package com.moyz.adi.common.vo;

import com.moyz.adi.common.helper.TtsModelContext;
import lombok.Data;

/**
 * TTS任务中的各种临时数据
 */
@Data
public class TtsJobInfo {
    /**
     * jobID
     */
    private String jobId;
    /**
     * tts模型上下文
     */
    private TtsModelContext ttsModelContext;
    /**
     * 文件路径
     */
    private String filePath;
}
