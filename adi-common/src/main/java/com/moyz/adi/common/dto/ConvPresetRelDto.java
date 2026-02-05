package com.moyz.adi.common.dto;

import lombok.Data;

/**
 * Conv预设Rel数据传输对象
 */
@Data
public class ConvPresetRelDto {
    /**
     * 主键ID
     */
    private Long id;
    /**
     * UUID
     */
    private String uuid;
    /**
     * 用户ConvID
     */
    private Long userConvId;
    /**
     * 预设ConvID
     */
    private Long presetConvId;
}
