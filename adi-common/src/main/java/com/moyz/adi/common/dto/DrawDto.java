package com.moyz.adi.common.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Draw数据传输对象
 */
@Data
public class DrawDto {
    /**
     * 主键ID
     */
    private Long id;
    /**
     * UUID
     */
    private String uuid;
    /**
     * 提示词
     */
    private String prompt;
    /**
     * AI模型名称
     */
    private String aiModelName;
    /**
     * interactingMethod
     */
    private Integer interactingMethod;
    /**
     * 是否公开
     */
    private Boolean isPublic;
    /**
     * star数量
     */
    private Integer starCount;
    /**
     * process状态
     */
    private Integer processStatus;
    /**
     * process状态描述
     */
    private String processStatusRemark;
    /**
     * generatedImages
     */
    @JsonIgnore
    private String generatedImages;
    /**
     * 用户ID
     */
    @JsonIgnore
    private Long userId;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
     * dynamic参数
     */
    private JsonNode dynamicParams;

    //非 Draw 字段
    /**
     * AI模型平台
     */
    private String aiModelPlatform;
    /**
     * 是否Star
     */
    private Boolean isStar;
    /**
     * original图片UUID
     */
    private String originalImageUuid;
    /**
     * original图片URL
     */
    private String originalImageUrl;
    /**
     * mask图片UUID
     */
    private String maskImageUuid;
    /**
     * mask图片URL
     */
    private String maskImageUrl;
    /**
     * 用户UUID
     */
    private String userUuid;
    /**
     * 用户名称
     */
    private String userName;
    /**
     * 图片Uuids
     */
    private List<String> imageUuids;
    /**
     * 图片Urls
     */
    private List<String> imageUrls;
}
