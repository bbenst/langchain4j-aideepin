package com.moyz.adi.common.vo;

import lombok.Data;

/**
 * 请求Rate限制对象
 */
@Data
public class RequestRateLimit {
    /**
     * times
     */
    private int times;
    /**
     * minutes
     */
    private int minutes;
    /**
     * 类型
     */
    private int type;
    /**
     * TYPE_TEXT
     */
    public static final int TYPE_TEXT = 1;
    /**
     * TYPE_IMAGE
     */
    public static final int TYPE_IMAGE = 2;
}
