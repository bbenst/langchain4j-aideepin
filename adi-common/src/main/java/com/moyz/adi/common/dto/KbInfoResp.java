package com.moyz.adi.common.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Kb信息响应
 */
@Data
public class KbInfoResp {
    /**
     * 主键ID
     */
    private Long id;
    /**
     * UUID
     */
    private String uuid;
    /**
     * 标题
     */
    private String title;
    /**
     * 描述
     */
    private String remark;
    /**
     * 是否公开
     */
    private Boolean isPublic;
    /**
     * 是否Strict
     */
    private Boolean isStrict;
    /**
     * star数量
     */
    private Integer starCount;
    /**
     * ingest最大Overlap
     */
    private Integer ingestMaxOverlap;
    /**
     * ingest模型名称
     */
    private String ingestModelName;
    /**
     * ingest模型ID
     */
    private Long ingestModelId;
    /**
     * ingestTokenEstimator
     */
    private String ingestTokenEstimator;
    /**
     * ingest向量模型
     */
    private String ingestEmbeddingModel;
    /**
     * retrieve最大Results
     */
    private Integer retrieveMaxResults;
    /**
     * retrieve最小分值
     */
    private Double retrieveMinScore;
    /**
     * 查询LlmTemperature
     */
    private Double queryLlmTemperature;
    /**
     * 查询系统消息
     */
    private String querySystemMessage;
    /**
     * ownerUUID
     */
    private String ownerUuid;
    /**
     * owner名称
     */
    private String ownerName;
    /**
     * item数量
     */
    private Integer itemCount;
    /**
     * 向量数量
     */
    private Integer embeddingCount;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
