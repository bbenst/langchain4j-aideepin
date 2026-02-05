package com.moyz.adi.common.dto;

import com.moyz.adi.common.enums.UserStatusEnum;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户信息数据传输对象
 */
@Data
public class UserInfoDto{
    /**
     * 主键ID
     */
    private Long id;
    /**
     * 名称
     */
    private String name;
    /**
     * 邮箱
     */
    private String email;
    /**
     * UUID
     */
    private String uuid;
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
     * 用户状态
     */
    private UserStatusEnum userStatus;
    /**
     * 激活时间
     */
    private LocalDateTime activeTime;
    /**
     * 是否管理员
     */
    private Boolean isAdmin;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
