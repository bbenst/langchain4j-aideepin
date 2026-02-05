package com.moyz.adi.common.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 工作流定义（用户定义的工作流）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("adi_workflow")
@Schema(title = "工作流定义 | workflow definition")
public class Workflow extends BaseEntity {
    /**
     * 序列化版本号
     */
    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * UUID
     */
    @TableField("uuid")
    private String uuid;
    /**
     * 标题
     */
    @TableField("title")
    private String title;
    /**
     * 描述
     */
    @TableField("remark")
    private String remark;
    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;
    /**
     * 是否公开
     */
    @TableField("is_public")
    private Boolean isPublic;
    /**
     * 是否启用
     */
    @TableField("is_enable")
    private Boolean isEnable;
}
