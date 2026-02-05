package com.moyz.adi.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * AI搜索Record响应
 */
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class AiSearchRecordResp {
    /**
     * UUID
     */
    private String uuid;
    /**
     * 问题
     */
    private String question;
    /**
     * 搜索EngineResp
     */
    private SearchEngineResp searchEngineResp;
    /**
     * 提示词
     */
    private String prompt;
    /**
     * 提示词Token
     */
    private Integer promptTokens;
    /**
     * 答案
     */
    private String answer;
    /**
     * 答案Token
     */
    private Integer answerTokens;
    /**
     * 用户UUID
     */
    private String userUuid;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
     * AI模型ID
     */
    private Long aiModelId;
    /**
     * AI模型平台
     */
    private String aiModelPlatform;
}
