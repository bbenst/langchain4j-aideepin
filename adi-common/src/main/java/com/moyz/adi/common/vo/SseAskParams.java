package com.moyz.adi.common.vo;

import com.moyz.adi.common.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;


/**
 * SSEAsk参数对象
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class SseAskParams {
    /**
     * 用户
     */
    private User user;
    //请求标识,如:知识库的记录uuid,搜索记录uuid
    /**
     * UUID
     */
    private String uuid;
    /**
     * 模型平台
     */
    private String modelPlatform;
    /**
     * 模型名称
     */
    private String modelName;
    /**
     * regenerate问题UUID
     */
    private String regenerateQuestionUuid;
    /**
     * 答案内容类型
     */
    private Integer answerContentType;
    /**
     * 语音
     */
    private String voice;
    /**
     * SSEEmitter
     */
    private SseEmitter sseEmitter;
    /**
     * 创建LLM时用到的属性，非必填
     */
    private ChatModelBuilderProperties modelProperties;
    /**
     * 进行http请求时最终提交给LLM的信息，必填
     */
    private ChatModelRequestParams httpRequestParams;
}
