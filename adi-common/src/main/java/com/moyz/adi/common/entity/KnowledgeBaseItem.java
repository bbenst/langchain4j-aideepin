package com.moyz.adi.common.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.moyz.adi.common.enums.EmbeddingStatusEnum;
import com.moyz.adi.common.enums.GraphicalStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 知识库-条目
 */
@Data
@TableName("adi_knowledge_base_item")
@Schema(title = "知识库条目实体", description = "知识库条目表")
public class KnowledgeBaseItem extends BaseEntity {
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
     * 名称
     */
    @Schema(title = "名称")
    @TableField("source_file_id")
    private Long sourceFileId;
    /**
     * UUID
     */
    @Schema(title = "uuid")
    @TableField("uuid")
    private String uuid;
    /**
     * 标题
     */
    @Schema(title = "标题")
    @TableField("title")
    private String title;
    /**
     * 内容摘要
     */
    @Schema(title = "内容摘要")
    @TableField("brief")
    private String brief;
    /**
     * 内容
     */
    @Schema(title = "内容")
    @TableField("remark")
    private String remark;
    /**
     * 向量化状态
     */
    @Schema(title = "向量化状态")
    @TableField("embedding_status")
    private EmbeddingStatusEnum embeddingStatus;
    /**
     * 向量化状态变更时间点
     */
    @Schema(title = "向量化状态变更时间点")
    @TableField("embedding_status_change_time")
    private LocalDateTime embeddingStatusChangeTime;
    /**
     * 图谱化状态
     */
    @Schema(title = "图谱化状态")
    @TableField("graphical_status")
    private GraphicalStatusEnum graphicalStatus;
    /**
     * 图谱化状态变更时间点
     */
    @Schema(title = "图谱化状态变更时间点")
    @TableField("graphical_status_change_time")
    private LocalDateTime graphicalStatusChangeTime;
}
