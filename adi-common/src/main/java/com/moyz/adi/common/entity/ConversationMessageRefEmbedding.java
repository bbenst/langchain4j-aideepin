package com.moyz.adi.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 会话消息-知识库的向量引用
 */
@Data
@TableName("adi_conversation_message_ref_embedding")
@Schema(title = "会话消息-知识库的向量-引用实体", description = "会话消息-知识库的向量-引用列表")
public class ConversationMessageRefEmbedding implements Serializable {
    /**
     * 序列化版本号
     */
    private static final long serialVersionUID = 1L;
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 消息ID
     */
    @Schema(title = "消息ID")
    @TableField("message_id")
    private Long messageId;
    /**
     * 向量id
     */
    @Schema(title = "向量id")
    @TableField("embedding_id")
    private String embeddingId;
    /**
     * 分数
     */
    @Schema(title = "分数")
    @TableField("score")
    private Double score;
    /**
     * 提问用户id
     */
    @Schema(title = "提问用户id")
    @TableField("user_id")
    private Long userId;
}
