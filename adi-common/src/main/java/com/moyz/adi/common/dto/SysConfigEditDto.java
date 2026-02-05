package com.moyz.adi.common.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

/**
 * Sys配置编辑数据传输对象
 */
@Data
@Validated
public class SysConfigEditDto {
    /**
     * 名称
     */
    @NotBlank
    private String name;
    /**
     * 值
     */
    @NotBlank
    private String value;
}
