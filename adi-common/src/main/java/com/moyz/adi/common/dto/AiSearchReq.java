package com.moyz.adi.common.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

/**
 * AI搜索请求
 */
@Validated
@Data
public class AiSearchReq {
    /**
     * 搜索Text
     */
    @NotBlank
    private String searchText;
    /**
     * engine名称
     */
    private String engineName;
    /**
     * 模型平台
     */
    private String modelPlatform;
    /**
     * 模型名称
     */
    private String modelName;
    /**
     * brief搜索
     */
    private boolean briefSearch;
}
