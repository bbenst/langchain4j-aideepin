package com.moyz.adi.common.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户每天消耗总量表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("adi_user_day_cost")
@Schema(title = "用户每天使用量")
public class UserDayCost extends BaseEntity {
    /**
     * 用户id
     */
    @Schema(title = "用户id")
    @TableField(value = "user_id")
    private Long userId;
    /**
     * 日期
     */
    @Schema(title = "日期")
    @TableField(value = "day")
    private Integer day;
    /**
     * 请求量
     */
    @Schema(title = "请求量")
    @TableField(value = "request_times")
    private Integer requestTimes;
    /**
     * token数量
     */
    @Schema(title = "token数量")
    @TableField(value = "tokens")
    private Integer tokens;
    /**
     * 绘图次数
     */
    @Schema(title = "绘图次数")
    @TableField(value = "draw_times")
    private Integer drawTimes;
    /**
     * 是：免费额度；否：收费额度
     */
    @Schema(title = "是：免费额度；否：收费额度")
    @TableField(value = "is_free")
    private Boolean isFree;
}
