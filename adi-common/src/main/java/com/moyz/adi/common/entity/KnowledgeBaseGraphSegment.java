package com.moyz.adi.common.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 知识库-图谱-文本块
 */
@Data
@TableName("adi_knowledge_base_graph_segment")
@Schema(title = "知识库-图谱-文本块", description = "知识库文本块表")
public class KnowledgeBaseGraphSegment extends BaseEntity {
    /**
     * 唯一标识
     */
    private String uuid;
    /**
     * 所属知识库uuid
     */
    @Schema(title = "所属知识库uuid")
    @TableField("kb_uuid")
    private String kbUuid;
    /**
     * 所属知识点uuid
     */
    @Schema(title = "所属知识点uuid")
    @TableField("kb_item_uuid")
    private String kbItemUuid;
    /**
     * 内容
     */
    @Schema(title = "内容")
    @TableField("remark")
    private String remark;
    /**
     * 创建用户id
     */
    @Schema(title = "创建用户id")
    @TableField("user_id")
    private Long userId;
}
