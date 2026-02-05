package com.moyz.adi.common.dto;

import lombok.Data;

/**
 * 提示词数据传输对象
 */
@Data
public class PromptDto {
    /**
     * 主键ID
     */
    private Long id;
    /**
     * act
     */
    private String act;
    /**
     * 提示词
     */
    private String prompt;
}
