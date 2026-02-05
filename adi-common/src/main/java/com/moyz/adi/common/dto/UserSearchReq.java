package com.moyz.adi.common.dto;

import com.moyz.adi.common.enums.UserStatusEnum;
import lombok.Data;

/**
 * 用户搜索请求
 */
@Data
public class UserSearchReq {
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
     * 用户状态
     */
    private Integer userStatus;
    /**
     * 是否管理员
     */
    private Boolean isAdmin;
    /**
     * 创建时间
     */
    private Long[] createTime;
    /**
     * 更新时间
     */
    private Long[] updateTime;
}
