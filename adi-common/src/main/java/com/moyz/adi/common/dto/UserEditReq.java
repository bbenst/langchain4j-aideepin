package com.moyz.adi.common.dto;

import com.moyz.adi.common.enums.UserStatusEnum;
import lombok.Data;

/**
 * 用户编辑请求
 */
@Data
public class UserEditReq {
    /**
     * UUID
     */
    private String uuid;
    /**
     * 名称
     */
    private String name;
    /**
     * 密码
     */
    private String password;
    /**
     * 用户状态
     */
    private UserStatusEnum userStatus;
    /**
     * 配额ByToken每日
     */
    private Integer quotaByTokenDaily;
    /**
     * 配额ByToken每月
     */
    private Integer quotaByTokenMonthly;
    /**
     * 配额By请求每日
     */
    private Integer quotaByRequestDaily;
    /**
     * 配额By请求每月
     */
    private Integer quotaByRequestMonthly;
    /**
     * 配额By图片每日
     */
    private Integer quotaByImageDaily;
    /**
     * 配额By图片每月
     */
    private Integer quotaByImageMonthly;
    /**
     * 是否管理员
     */
    private Boolean isAdmin;
}
