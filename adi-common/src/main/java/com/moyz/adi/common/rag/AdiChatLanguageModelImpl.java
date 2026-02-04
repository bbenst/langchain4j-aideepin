package com.moyz.adi.common.rag;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;

import java.util.List;
import java.util.function.Consumer;

/**
 * ChatModel 的包装实现，用于在响应后执行回调。
 */
public class AdiChatLanguageModelImpl implements ChatModel {

    /**
     * 响应回调处理器。
     */
    private final Consumer<ChatResponse> consumer;

    /**
     * 构建包装模型。
     *
     * @param ChatModel 原始模型（保留参数以兼容外部调用）
     * @param consumer 响应回调
     */
    public AdiChatLanguageModelImpl(ChatModel ChatModel, Consumer<ChatResponse> consumer) {
        this.consumer = consumer;
    }

    /**
     * 处理多条消息对话并执行回调。
     *
     * @param messages 消息数组
     * @return 模型响应
     */
    @Override
    public ChatResponse chat(ChatMessage... messages) {
        ChatResponse chatResponse = ChatModel.super.chat(messages);
        consumer.accept(chatResponse);
        return chatResponse;
    }

    /**
     * 处理消息列表对话并执行回调。
     *
     * @param messages 消息列表
     * @return 模型响应
     */
    @Override
    public ChatResponse chat(List<ChatMessage> messages) {
        ChatResponse chatResponse = ChatModel.super.chat(messages);
        consumer.accept(chatResponse);
        return chatResponse;
    }

    /**
     * 处理请求对象并执行回调。
     *
     * @param chatRequest 请求对象
     * @return 模型响应
     */
    @Override
    public ChatResponse chat(ChatRequest chatRequest) {
        ChatResponse chatResponse = ChatModel.super.chat(chatRequest);
        consumer.accept(chatResponse);
        return chatResponse;
    }

}
