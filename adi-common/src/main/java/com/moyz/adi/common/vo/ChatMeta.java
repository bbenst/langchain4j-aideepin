package com.moyz.adi.common.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 聊天Meta对象
 */
@Data
@AllArgsConstructor
public class ChatMeta {
    /**
     * 问题
     */
    private PromptMeta question;
    /**
     * 答案
     */
    private AnswerMeta answer;
    /**
     * 音频信息
     */
    private AudioInfo audioInfo;
}
