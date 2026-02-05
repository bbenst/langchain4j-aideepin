package com.moyz.adi.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 知识库-提问记录-图谱引用记录
 */
@Data
@TableName("adi_knowledge_base_qa_ref_graph")
@Schema(title = "知识库问答记录-图谱引用", description = "知识库问答记录-图谱引用列表")
public class KnowledgeBaseQaRefGraph implements Serializable {
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
     * 从用户问题中解析出来的实体
     */
    @Schema(title = "从用户问题中解析出来的实体")
    @TableField("entities_from_question")
    private String entitiesFromQuestion;
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
