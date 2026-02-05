package com.moyz.adi.common.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

/**
 * Kb编辑请求
 */
@Data
@Validated
public class KbEditReq {
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
    @NotBlank
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
     * retrieve最大Results
     */
    private Integer retrieveMaxResults;
    /**
     * retrieve最小分值
     */
    private Double retrieveMinScore;
    /**
     * ingest最大Overlap
     */
    private Integer ingestMaxOverlap;
    /**
     * ingest模型ID
     */
    private Long ingestModelId;
    /**
     * ingestTokenEstimator
     */
    private String ingestTokenEstimator;
    /**
     * 查询LlmTemperature
     */
    private Double queryLlmTemperature;
    /**
     * 查询系统消息
     */
    private String querySystemMessage;
}
