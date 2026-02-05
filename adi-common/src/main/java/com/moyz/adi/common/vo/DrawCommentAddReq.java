package com.moyz.adi.common.vo;

import lombok.Data;

/**
 * DrawComment新增请求
 */
@Data
public class DrawCommentAddReq {
    /**
     * drawUUID
     */
    private String drawUuid;
    /**
     * comment
     */
    private String comment;
}
