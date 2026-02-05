package com.moyz.adi.common.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * 提示词Meta对象
 */
@Data
@Builder
@AllArgsConstructor
public class PromptMeta {
    /**
     * Token
     */
    private Integer tokens;
    /**
     * UUID
     */
    private String uuid;
}
