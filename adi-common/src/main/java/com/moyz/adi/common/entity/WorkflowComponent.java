package com.moyz.adi.common.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 工作流组件
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "adi_workflow_component", autoResultMap = true)
@Schema(title = "工作流组件")
public class WorkflowComponent extends BaseEntity {
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
     * 名称
     */
    @TableField("name")
    private String name;
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
     * display排序
     */
    @TableField("display_order")
    private Integer displayOrder;
    /**
     * 是否启用
     */
    @TableField("is_enable")
    private Boolean isEnable;
}
