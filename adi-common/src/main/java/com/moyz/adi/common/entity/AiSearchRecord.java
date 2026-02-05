package com.moyz.adi.common.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.moyz.adi.common.base.SearchEngineRespTypeHandler;
import com.moyz.adi.common.dto.SearchEngineResp;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.apache.ibatis.type.JdbcType;

/**
 * 搜索记录
 */
@Data
@TableName("adi_ai_search_record")
@Schema(title = "AiSearchRecord对象", description = "AI搜索记录表")
public class AiSearchRecord extends BaseEntity {
    /**
     * UUID
     */
    @TableField("uuid")
    private String uuid;
    /**
     * 问题
     */
    @Schema(title = "问题")
    @TableField("question")
    private String question;
    /**
     * 搜索引擎的响应内容
     */
    @Schema(title = "Search engine's response content")
    @TableField(value = "search_engine_response", jdbcType = JdbcType.JAVA_OBJECT, typeHandler = SearchEngineRespTypeHandler.class)
    private SearchEngineResp searchEngineResp;
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
    @TableField("prompt_tokens")
    private Integer promptTokens;
    /**
     * 答案
     */
    @Schema(title = "答案")
    @TableField("answer")
    private String answer;
    /**
     * 答案消耗的token
     */
    @Schema(title = "答案消耗的token")
    @TableField("answer_tokens")
    private Integer answerTokens;
    /**
     * 提问用户uuid
     */
    @Schema(title = "提问用户uuid")
    @TableField("user_uuid")
    private String userUuid;
    /**
     * 提问用户id
     */
    @Schema(title = "提问用户id")
    @TableField("user_id")
    private Long userId;
    /**
     * AI模型ID
     */
    @Schema(title = "adi_ai_model id")
    @TableField("ai_model_id")
    private Long aiModelId;
}
