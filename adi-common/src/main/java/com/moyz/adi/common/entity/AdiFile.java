package com.moyz.adi.common.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 文件
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("adi_file")
@Schema(title = "文件表")
public class AdiFile extends BaseEntity {
    /**
     * 用户id
     */
    @Schema(title = "用户id")
    @TableField(value = "user_id")
    private Long userId;
    /**
     * 文件名
     */
    @Schema(title = "name")
    @TableField(value = "name")
    private String name;
    /**
     * 文件的UUID
     */
    @Schema(title = "uuid")
    @TableField(value = "uuid")
    private String uuid;
    /**
     * 文件的哈希值
     */
    @Schema(title = "sha256")
    @TableField(value = "sha256")
    private String sha256;
    /**
     * 文件扩展名
     */
    @Schema(title = "file extension")
    @TableField(value = "ext")
    private String ext;
    /**
     * 路径
     */
    @Schema(title = "路径")
    @TableField(value = "path")
    private String path;
    /**
     * 存储位置，1：本地存储，2：阿里云OSS
     */
    @Schema(title = "存储位置，1：本地存储，2：阿里云OSS")
    @TableField(value = "storage_location")
    private Integer storageLocation;
    /**
     * 引用数量
     */
    @Schema(title = "引用数量")
    @TableField(value = "ref_count")
    private Integer refCount;

}
