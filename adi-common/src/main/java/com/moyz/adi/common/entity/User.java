package com.moyz.adi.common.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.moyz.adi.common.enums.UserStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 用户表
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("adi_user")
@Schema(title = "User对象")
public class User extends BaseEntity {
    /**
     * 用户名称
     */
    @Schema(name = "用户名称")
    @TableField("name")
    private String name;
    /**
     * 用户邮箱
     */
    @TableField("email")
    private String email;
    /**
     * 密码
     */
    @TableField("password")
    private String password;
    /**
     * 用户的UUID
     */
    @TableField("uuid")
    private String uuid;
    /**
     * 上下文理解中需要携带的消息对数量（提示词及回复）
     */
    @Schema(name = "上下文理解中需要携带的消息对数量（提示词及回复）")
    @TableField("understand_context_msg_pair_num")
    private Integer understandContextMsgPairNum;
    /**
     * 每日token配额
     */
    @Schema(name = "token quota in one day")
    @TableField("quota_by_token_daily")
    private Integer quotaByTokenDaily;
    /**
     * 每月token配额
     */
    @Schema(name = "token quota in one month")
    @TableField("quota_by_token_monthly")
    private Integer quotaByTokenMonthly;
    /**
     * 每日请求配额
     */
    @Schema(name = "request quota in one day")
    @TableField("quota_by_request_daily")
    private Integer quotaByRequestDaily;
    /**
     * 每月请求配额
     */
    @Schema(name = "request quota in one month")
    @TableField("quota_by_request_monthly")
    private Integer quotaByRequestMonthly;
    /**
     * 每日图片配额
     */
    @TableField("quota_by_image_daily")
    private Integer quotaByImageDaily;
    /**
     * 每月图片配额
     */
    @TableField("quota_by_image_monthly")
    private Integer quotaByImageMonthly;
    /**
     * 用户状态，1：待验证；2：正常；3：冻结
     */
    @TableField("user_status")
    private UserStatusEnum userStatus;
    /**
     * 激活时间
     */
    @TableField("active_time")
    private LocalDateTime activeTime;
    /**
     * 是否管理员（0：否，1：是）
     */
    @Schema(title = "是否管理员（0：否，1：是）")
    @TableField(value = "is_admin")
    private Boolean isAdmin;
}
