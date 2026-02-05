package com.moyz.adi.common.dto;

import lombok.Data;

/**
 * Sys配置数据传输对象
 */
@Data
public class SysConfigDto {
    /**
     * 名称
     */
    private String name;
    /**
     * 值
     */
    private String value;
}
