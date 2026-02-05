package com.moyz.adi.common.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 预设会话与用户会话关系表
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("adi_conversation_preset_rel")
@Schema(title = "预设对话与用户会话关系实体", description = "预设对话与用户会话关系表")
public class ConversationPresetRel extends BaseEntity {
    /**
     * UUID
     */
    private String uuid;
    /**
     * 用户ID
     */
    private Long userId;
    /**
     * 预设会话ID
     */
    private Long presetConvId;
    /**
     * 用户会话ID
     */
    private Long userConvId;
}
