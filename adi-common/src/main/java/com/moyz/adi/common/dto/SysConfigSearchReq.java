package com.moyz.adi.common.dto;

import lombok.Data;

import java.util.List;

/**
 * Sys配置搜索请求
 */
@Data
public class SysConfigSearchReq {
    /**
     * keyword
     */
    private String keyword;
    /**
     * names
     */
    private List<String> names;
}
