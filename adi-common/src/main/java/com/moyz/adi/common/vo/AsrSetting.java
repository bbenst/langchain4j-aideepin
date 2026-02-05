package com.moyz.adi.common.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Asr配置对象
 */
@Data
public class AsrSetting {
    /**
     * 模型名称
     */
    @JsonProperty("model_name")
    private String modelName;
    /**
     * 平台
     */
    private String platform;
    /**
     * 最大RecordDuration
     */
    @JsonProperty("max_record_duration")
    private int maxRecordDuration;
    /**
     * 最大文件数量
     */
    @JsonProperty("max_file_size")
    private int maxFileSize;
}
