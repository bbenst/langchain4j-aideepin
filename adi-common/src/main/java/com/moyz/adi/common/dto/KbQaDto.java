package com.moyz.adi.common.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * KbQa数据传输对象
 */
@Data
public class KbQaDto {
    /**
     * UUID
     */
    @Schema(title = "uuid")
    private String uuid;
    /**
     * 知识库uuid
     */
    @Schema(title = "知识库uuid")
    private String kbUuid;
    /**
     * 来源文档id,以逗号隔开
     */
    @Schema(title = "来源文档id,以逗号隔开")
    private String sourceFileIds;
    /**
     * 问题
     */
    @Schema(title = "问题")
    private String question;
    /**
     * 最终提供给LLM的提示词
     */
    @Schema(title = "最终提供给LLM的提示词")
    @TableField("prompt")
    private String prompt;
    /**
     * 提供给LLM的提示词所消耗的token数量
     */
    @Schema(title = "提供给LLM的提示词所消耗的token数量")
    private Integer promptTokens;
    /**
     * 答案
     */
    @Schema(title = "答案")
    private String answer;
    /**
     * 答案消耗的token
     */
    @Schema(title = "答案消耗的token")
    private Integer answerTokens;
    /**
     * AI模型ID
     */
    @Schema(title = "ai model id")
    private Long aiModelId;
    /**
     * AI模型平台
     */
    @Schema(title = "ai model platform")
    private String aiModelPlatform;
}
