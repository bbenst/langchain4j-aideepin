package com.moyz.adi.common.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * DrawStar对象
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("adi_draw_star")
@Schema(title = "Favorite draws", description = "Favorite draws")
public class DrawStar extends BaseEntity {
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
}
