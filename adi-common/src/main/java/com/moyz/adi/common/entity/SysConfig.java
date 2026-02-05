package com.moyz.adi.common.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 系统配置表
 */
@Data
@TableName("adi_sys_config")
@Schema(title = "系统配置表")
public class SysConfig extends BaseEntity {
    /**
     * 配置名称
     */
    @Schema(title = "配置名称")
    @TableField("name")
    private String name;
    /**
     * 配置项的值
     */
    @Schema(title = "配置项的值")
    private String value;

}
