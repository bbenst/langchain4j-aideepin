package com.moyz.adi.common.dto;

import lombok.Data;

import java.util.List;

/**
 * Draw列表响应
 */
@Data
public class DrawListResp {
    /**
     * 最小ID
     */
    private Long minId;
    /**
     * draws
     */
    private List<DrawDto> draws;
}
