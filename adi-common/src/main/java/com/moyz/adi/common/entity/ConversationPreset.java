package com.moyz.adi.common.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 预设会话(角色)表
 */
@Data
@TableName("adi_conversation_preset")
@Schema(title = "预设对话实体", description = "预设对话表")
public class ConversationPreset extends BaseEntity {
    /**
     * UUID
     */
    private String uuid;
    /**
     * 标题
     */
    private String title;
    /**
     * 描述
     */
    private String remark;
    /**
     * 提供给LLM的系统信息
     */
    private String aiSystemMessage;
}
