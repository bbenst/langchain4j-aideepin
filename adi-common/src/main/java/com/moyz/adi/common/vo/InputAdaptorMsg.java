package com.moyz.adi.common.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 调整LLM的输入时产生的消息
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class InputAdaptorMsg {
    /**
     * TOKEN_TOO_MUCH_NOT
     */
    public static final int TOKEN_TOO_MUCH_NOT = 0;
    /**
     * TOKEN_TOO_MUCH_QUESTION
     */
    public static final int TOKEN_TOO_MUCH_QUESTION = 1;
    /**
     * TOKEN_TOO_MUCH_MEMORY
     */
    public static final int TOKEN_TOO_MUCH_MEMORY = 2;
    /**
     * TOKEN_TOO_MUCH_RETRIEVE_DOCS
     */
    public static final int TOKEN_TOO_MUCH_RETRIEVE_DOCS = 3;
    /**
     * TOKEN_TOO_MUCH_MESSAGE
     */
    public static final int TOKEN_TOO_MUCH_MESSAGE = 4;
    /**
     * TokenTooMuch
     */
    private int tokenTooMuch = TOKEN_TOO_MUCH_NOT;
    /**
     * 用户问题Token数量
     */
    private int userQuestionTokenCount;
    /**
     * memoryToken数量
     */
    private int memoryTokenCount;
    /**
     * retrievedDocsToken数量
     */
    private int retrievedDocsTokenCount;
    /**
     * messagesToken数量
     */
    private int messagesTokenCount;
}
