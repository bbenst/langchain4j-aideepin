package com.moyz.adi.common.dto;

import lombok.Data;

import java.util.List;

/**
 * 提示词列表响应
 */
@Data
public class PromptListResp {
    /**
     * 最大更新时间
     */
    private String maxUpdateTime;
    /**
     * prompts
     */
    private List<PromptDto> prompts;
}
