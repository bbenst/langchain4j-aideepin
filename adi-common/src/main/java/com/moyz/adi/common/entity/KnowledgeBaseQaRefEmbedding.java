package com.moyz.adi.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 知识库-提问记录-向量引用列表
 */
@Data
@TableName("adi_knowledge_base_qa_ref_embedding")
@Schema(title = "知识库问答记录-引用实体", description = "知识库问答记录-引用列表")
public class KnowledgeBaseQaRefEmbedding implements Serializable {
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
     * 问答记录ID
     */
    @Schema(title = "问答记录ID")
    @TableField("qa_record_id")
    private Long qaRecordId;
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
