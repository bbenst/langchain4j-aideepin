package com.moyz.adi.common.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DrawComment数据传输对象
 */
@Data
@Builder
public class DrawCommentDto {
    /**
     * UUID
     */
    private String uuid;
    /**
     * 用户UUID
     */
    private String userUuid;
    /**
     * 用户名称
     */
    private String userName;
    /**
     * drawUUID
     */
    private String drawUuid;
    /**
     * 描述
     */
    private String remark;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
