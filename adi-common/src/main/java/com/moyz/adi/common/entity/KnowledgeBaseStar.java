package com.moyz.adi.common.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 知识库-点赞记录
 */
@Data
@TableName("adi_knowledge_base_star")
@Schema(title = "知识库点赞记录实体", description = "知识库点赞记录表")
public class KnowledgeBaseStar extends BaseEntity {
    /**
     * 知识库ID
     */
    @Schema(title = "Knowledge base id")
    @TableField("kb_id")
    private Long kbId;
    /**
     * 知识库UUID
     */
    @Schema(title = "Knowledge base uuid")
    @TableField("kb_uuid")
    private String kbUuid;
    /**
     * 用户ID
     */
    @Schema(title = "User id")
    @TableField("user_id")
    private Long userId;
    /**
     * 用户UUID
     */
    @Schema(title = "User uuid")
    @TableField("user_uuid")
    private String userUuid;

}
