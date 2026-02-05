package com.moyz.adi.common.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.apache.ibatis.type.JdbcType;

/**
 * 知识库-提问记录
 */
@Data
@TableName("adi_knowledge_base_qa")
@Schema(title = "知识库问答记录实体", description = "知识库问答记录表")
public class KnowledgeBaseQa extends BaseEntity {
    /**
     * UUID
     */
    @Schema(title = "uuid")
    @TableField(value = "uuid", jdbcType = JdbcType.VARCHAR)
    private String uuid;
    /**
     * 知识库id
     */
    @Schema(title = "知识库id")
    @TableField("kb_id")
    private Long kbId;
    /**
     * 知识库uuid
     */
    @Schema(title = "知识库uuid")
    @TableField("kb_uuid")
    private String kbUuid;
    /**
     * 来源文档id,以逗号隔开
     */
    @Schema(title = "来源文档id,以逗号隔开")
    @TableField("source_file_ids")
    private String sourceFileIds;
    /**
     * 问题
     */
    @Schema(title = "问题")
    @TableField("question")
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
