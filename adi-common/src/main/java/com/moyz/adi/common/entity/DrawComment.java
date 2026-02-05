package com.moyz.adi.common.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * DrawComment对象
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("adi_draw_comment")
@Schema(title = "Draw comment", description = "Draw comment")
public class DrawComment extends BaseEntity {
    /**
     * UUID
     */
    @TableField("uuid")
    private String uuid;
    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;
    /**
     * drawID
     */
    @TableField("draw_id")
    private Long drawId;
    /**
     * 描述
     */
    @TableField("remark")
    private String remark;
}
