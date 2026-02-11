package com.moyz.adi.common.util;

import com.moyz.adi.common.rag.TokenEstimatorThreadLocal;
import com.moyz.adi.common.rag.TokenEstimatorFactory;
import com.moyz.adi.common.vo.InputAdaptorMsg;
import dev.langchain4j.data.message.*;
import dev.langchain4j.model.TokenCountEstimator;
import dev.langchain4j.rag.AugmentationRequest;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.query.Metadata;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
/**
 * 根据模型的最大输入 token 数，自动调整召回文档与历史记录长度。
 */
@Slf4j
public class InputAdaptor {
    /**
     * 校验用户问题是否超过最大 token 数。
     *
     * @param userQuestion  用户问题
     * @param maxInputTokens 最大输入 token 数
     * @return 校验结果
     */
    public static InputAdaptorMsg isQuestionValid(String userQuestion, int maxInputTokens) {
        // 默认使用线程上下文的估算器，保证与当前知识库配置一致
        return isQuestionValid(userQuestion, maxInputTokens, TokenEstimatorFactory.create(TokenEstimatorThreadLocal.getTokenEstimator()));
    }
    /**
     * 使用指定 tokenizer 校验用户问题长度。
     *
     * @param userQuestion  用户问题
     * @param maxInputTokens 最大输入 token 数
     * @param tokenizer     token 估算器
     * @return 校验结果
     */
    public static InputAdaptorMsg isQuestionValid(String userQuestion, int maxInputTokens, TokenCountEstimator tokenizer) {
        InputAdaptorMsg result = new InputAdaptorMsg();
        result.setTokenTooMuch(InputAdaptorMsg.TOKEN_TOO_MUCH_NOT);

        // 先估算问题 token 数，超过窗口则直接标记
        int questionLength = tokenizer.estimateTokenCountInText(userQuestion);
        result.setUserQuestionTokenCount(questionLength);
        if (questionLength > maxInputTokens) {
            log.warn("用户问题过长,已超过{}个token", maxInputTokens);
            result.setTokenTooMuch(InputAdaptorMsg.TOKEN_TOO_MUCH_QUESTION);
        }
        return result;
    }

    /**
     * 调整携带的历史记录。
     * 请求 token 可能超长场景之一（请求压缩）：用户原始问题 + 历史记录。
     *
     * @param augmentationRequest 增强请求
     * @param maxInputTokens      最大输入 token 数
     * @param tokenCostConsumer   token 消耗回调
     * @return 调整后的元数据
     * @deprecated 请使用 dev.langchain4j.memory.chatTokenWindowChatMemory
     */
    @Deprecated
    public static Metadata adjustMemory(AugmentationRequest augmentationRequest, int maxInputTokens, Consumer<InputAdaptorMsg> tokenCostConsumer) {
//        ChatMessage chatMessage = augmentationRequest.chatMessage();
//        Metadata metadata = augmentationRequest.metadata();
//
//        String tokenizerName = TokenEstimatorThreadLocal.getTokenEstimator();
//        TokenCountEstimator tokenizer = TokenEstimatorFactory.create(tokenizerName);
//        InputAdaptorMsg inputAdaptorMsg = isQuestionValid(chatMessage.toString(), maxInputTokens, tokenizer);
//        if (inputAdaptorMsg.getTokenTooMuch() == InputAdaptorMsg.TOKEN_TOO_MUCH_QUESTION) {
//            tokenCostConsumer.accept(inputAdaptorMsg);
//        }
//        // 计算准备待增强的用户原始问题及历史记录的长度,如果超额，则丢弃部分或全部历史记录
//        // 对用户问题进行增强是为了更好地召回文档
//        List<ChatMessage> validMemories = new ArrayList<>();
//        int allMemoryTokenCount = 0;
//        int tokenTooMuch = InputAdaptorMsg.TOKEN_TOO_MUCH_NOT;
//        for (int i = metadata.chatMemory().size() - 1; i >= 0; i--) {
//            String memory = metadata.chatMemory().get(i).text();
//            int currentMemoryTokenCount = tokenizer.estimateTokenCountInText(memory);
//            if (inputAdaptorMsg.getUserQuestionTokenCount() + allMemoryTokenCount + currentMemoryTokenCount < maxInputTokens) {
//                allMemoryTokenCount += currentMemoryTokenCount;
//                validMemories.add(metadata.chatMemory().get(i));
//            } else {
//                tokenTooMuch = InputAdaptorMsg.TOKEN_TOO_MUCH_MEMORY;
//                log.warn("记忆内容过长,丢弃\n>>>>> {} <<<<<", memory.substring(0, Math.min(memory.length(), 30)));
//            }
//        }
//        //重新排序及写入内容适量的记忆
//        Collections.reverse(validMemories);
//
//        inputAdaptorMsg.setTokenTooMuch(tokenTooMuch);
//        inputAdaptorMsg.setMemoryTokenCount(allMemoryTokenCount);
//        tokenCostConsumer.accept(inputAdaptorMsg);
//
//        return Metadata.from(metadata.userMessage(), metadata.chatMemoryId(), validMemories);

        // 已废弃方法，保留占位实现，避免误用
        return null;
    }

    /**
     * 调整召回文档。
     * 请求 token 超长场景之二（召回文档成功后，准备结合历史记录前）：原始用户问题 + 召回文档。
     *
     * @param questionLength 原始用户问题 token 数
     * @param contents       召回文档内容
     * @param maxInputTokens 最大输入 token 数
     * @return 截取后的文档内容
     */
    public static List<Content> adjustRetrieveDocs(int questionLength, List<Content> contents, int maxInputTokens) {
        if (contents.isEmpty()) {
            log.info("文档数量为0");
            return Collections.emptyList();
        }
        String tokenizerName = TokenEstimatorThreadLocal.getTokenEstimator();
        TokenCountEstimator tokenizer = TokenEstimatorFactory.create(tokenizerName);
        // 计算原始问题与召回文档的 token 总量，超限则逐步丢弃
        int allRetrievedDocsTokenCount = 0;
        List<Content> validContents = new ArrayList<>();
        for (Content content : contents) {
            // 按文档粒度累加预算，确保不超过模型窗口
            int currentDocTokenCount = tokenizer.estimateTokenCountInText(content.textSegment().text());
            if (questionLength + allRetrievedDocsTokenCount + currentDocTokenCount < maxInputTokens) {
                allRetrievedDocsTokenCount += currentDocTokenCount;
                validContents.add(content);
            } else {
                log.warn("召回文档太长,丢弃\n>>>>> {} <<<<<", content.textSegment().text().substring(0, Math.min(content.textSegment().text().length(), 30)));
            }
        }
        log.info("文档token数:{}", allRetrievedDocsTokenCount);
        return validContents;
    }

    /**
     * 调整准备向 LLM 请求的消息数量以适配最大输入 token 数。
     * 请求 token 超长场景之三（召回成功后，结合用户问题、历史记录提交给 LLM 前）：原始用户问题 + 召回文档。
     *
     * @param messages       消息列表
     * @param maxInputTokens 最大输入 token 数
     * @return 调整后的消息列表
     */
    @Deprecated
    public static List<ChatMessage> adjustMessages(List<ChatMessage> messages, int maxInputTokens) {
        String tokenizerName = TokenEstimatorThreadLocal.getTokenEstimator();
        TokenCountEstimator tokenizer = TokenEstimatorFactory.create(tokenizerName);
        // 兼容历史实现，保留方法签名以避免外部调用失败
        int messageSize = messages.size();
        ChatMessage latestMessage = messages.get(messageSize - 1);
        List<ChatMessage> result = new ArrayList<>();
//        //最新一条消息（即当前用户的提问）必须留下
//        result.add(latestMessage);
//        int allTokenCount = 0;
//        if (latestMessage instanceof UserMessage userMessage && userMessage.contents().get(0) instanceof TextContent textContent) {
//            allTokenCount += tokenizer.estimateTokenCountInText(textContent.text());
//        }
//        for (int i = messageSize - 1 - 1; i >= 0; i--) {
//            log.info("messageSize i:{}", i);
//            ChatMessage curMsg = messages.get(i);
//            //多模态时，先不计算token
//            if (curMsg instanceof UserMessage && ((UserMessage) curMsg).contents().stream().anyMatch(item -> item instanceof ImageContent)) {
//                result.add(curMsg);
//            } else {
//
//                log.info("messageSize allTokenCount:{}", allTokenCount);
//                int currentMessageTokenCount = tokenizer.estimateTokenCountInText(curMsg.text());
//                if (allTokenCount + currentMessageTokenCount < maxInputTokens) {
//                    allTokenCount += currentMessageTokenCount;
//                    result.add(curMsg);
//                } else {
//                    log.warn("消息过长,丢弃\n>>>>> {} <<<<<", curMsg.text().substring(0, Math.min(curMsg.text().length(), 30)));
//                    //如果当前是AI的回复，把对应的用户提问也丢弃
//                    if (curMsg instanceof AiMessage) {
//                        i--;
//                        curMsg = messages.get(i);
//                        if (null != curMsg) {
//                            log.warn("对应的用户问题一并丢弃\n>>>>> {} <<<<<", curMsg.text().substring(0, Math.min(curMsg.text().length(), 30)));
//                        }
//                    }
//                }
//            }
//
//        }
//        Collections.reverse(result);
        return result;
    }
}
