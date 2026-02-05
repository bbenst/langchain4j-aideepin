package com.moyz.adi.common.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 搜索请求
 */
@Data
public class SearchReq {
    /**
     * keyword
     */
    @NotBlank
    private String keyword;
}
