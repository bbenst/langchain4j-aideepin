package com.moyz.adi.common.dto;

import lombok.Data;

/**
 * Conv知识对象
 */
@Data
public class ConvKnowledge {
    /**
     * 主键ID
     */
    private Long id;
    /**
     * UUID
     */
    private String uuid;
    /**
     * 标题
     */
    private String title;
    /**
     * 是否Mine
     */
    private Boolean isMine;
    /**
     * 是否公开
     */
    private Boolean isPublic;
    /**
     * kb信息
     */
    private KbInfoResp kbInfo;
    /**
     * 是否启用
     */
    private Boolean isEnable;
}
