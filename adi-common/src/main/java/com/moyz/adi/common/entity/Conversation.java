package com.moyz.adi.common.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.moyz.adi.common.base.AudioConfigTypeHandler;
import com.moyz.adi.common.vo.AudioConfig;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.JdbcType;

/**
 * 对话实体
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName(value = "adi_conversation", autoResultMap = true)
@Schema(title = "对话实体", description = "对话表")
public class Conversation extends BaseEntity {
    /**
     * 用户id
     */
    @Schema(title = "用户id")
    @TableField("user_id")
    private Long userId;
    /**
     * 对话uuid
     */
    @Schema(title = "对话uuid")
    @TableField("uuid")
    private String uuid;
    /**
     * 会话标题
     */
    @Schema(title = "会话标题")
    @TableField("title")
    private String title;
    /**
     * 描述
     */
    @Schema(title = "描述")
    @TableField("remark")
    private String remark;
    /**
     * 消耗的token数量
     */
    @Schema(title = "消耗的token数量")
    @TableField("tokens")
    private Integer tokens;
    /**
     * 是否开启理解上下文的功能
     */
    @Schema(name = "是否开启理解上下文的功能")
    @TableField("understand_context_enable")
    private Boolean understandContextEnable;
    /**
     * 设置系统信息(角色设定内容) | Set the system message to ai, ig: you are a lawyer
     */
    @Schema(title = "设置系统信息(角色设定内容) | Set the system message to ai, ig: you are a lawyer")
    @TableField("ai_system_message")
    private String aiSystemMessage;
    /**
     * 请求LLM时的temperature
     */
    @Schema(title = "请求LLM时的temperature")
    @TableField("llm_temperature")
    private Double llmTemperature;
    /**
     * 启用的mcp服务id列表
     */
    @Schema(title = "启用的mcp服务id列表")
    @TableField("mcp_ids")
    private String mcpIds;
    /**
     * 关联使用的知识库id列表
     */
    @Schema(title = "关联使用的知识库id列表")
    @TableField("kb_ids")
    private String kbIds;
    /**
     * 设置响应内容类型：1：自动（跟随用户的输入类型，如果用户输入是音频，则响应内容也同样是音频，如果用户输入是文本，则响应内容显示文本），2：文本，3：音频
     */
    @Schema(title = "设置响应内容类型：1：自动（跟随用户的输入类型，如果用户输入是音频，则响应内容也同样是音频，如果用户输入是文本，则响应内容显示文本），2：文本，3：音频")
    @TableField("answer_content_type")
    private Integer answerContentType;
    /**
     * 设置聊天时音频类型的响应内容是否自动播放
     */
    @Schema(title = "设置聊天时音频类型的响应内容是否自动播放")
    @TableField("is_autoplay_answer")
    private Boolean isAutoplayAnswer;
    /**
     * 是否启用思考功能
     */
    @Schema(title = "是否启用思考功能")
    @TableField("is_enable_thinking")
    private Boolean isEnableThinking;
    /**
     * 是否启用网络搜索功能
     */
    @Schema(title = "是否启用网络搜索功能")
    @TableField("is_enable_web_search")
    private Boolean isEnableWebSearch;
    /**
     * 音频配置
     */
    @TableField(value = "audio_config", jdbcType = JdbcType.JAVA_OBJECT, typeHandler = AudioConfigTypeHandler.class)
    private AudioConfig audioConfig;
}
