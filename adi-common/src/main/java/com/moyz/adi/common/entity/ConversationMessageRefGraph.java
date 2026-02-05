package com.moyz.adi.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 会话消息-知识库的图谱引用记录
 */
@Data
@TableName("adi_conversation_message_ref_graph")
@Schema(title = "会话消息-知识库-图谱引用", description = "会话消息-知识库-图谱引用列表")
public class ConversationMessageRefGraph implements Serializable {
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
     * LLM解析出来的图谱
     */
    @Schema(title = "LLM解析出来的图谱")
    @TableField("graph_from_llm")
    private String graphFromLlm;
    /**
     * 从图数据库中查找得到的图谱
     */
    @Schema(title = "从图数据库中查找得到的图谱")
    @TableField("graph_from_store")
    private String graphFromStore;
    /**
     * 提问用户id
     */
    @Schema(title = "提问用户id")
    @TableField("user_id")
    private Long userId;
}
