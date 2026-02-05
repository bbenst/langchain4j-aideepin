package com.moyz.adi.common.dto;

import com.moyz.adi.common.annotation.AskReqCheck;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.util.List;

/**
 * Ask请求
 */
@Schema(description = "对话的请求对象")
@Data
@AskReqCheck
public class AskReq {
    /**
     * conversationUUID
     */
    @Length(min = 32, max = 32)
    private String conversationUuid;
    /**
     * parent消息ID
     */
    private String parentMessageId;
    /**
     * 提示词
     */
    private String prompt;

    //语音聊天时产生的音频文件uuid
    /**
     * 音频UUID
     */
    private String audioUuid;
    //语音聊天时产生的音频时长，单位秒
    /**
     * 音频Duration
     */
    private Integer audioDuration;
    /**
     * 图片地址，多模态LLM使用，目前只支持本地图片uuid
     */
    private List<String> imageUrls;
    /**
     * regenerate问题UUID
     */
    private String regenerateQuestionUuid;
    /**
     * 模型平台
     */
    private String modelPlatform;
    /**
     * 模型名称
     */
    private String modelName;

    //后端用的临时变量
    /**
     * processed提示词
     */
    private String processedPrompt;
}
