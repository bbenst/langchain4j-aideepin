package com.moyz.adi.common.rag;

import com.moyz.adi.common.cosntant.AdiConstant;
import com.moyz.adi.common.entity.AiModel;
import com.moyz.adi.common.helper.LLMContext;
import com.moyz.adi.common.languagemodel.AbstractLLMService;
import dev.langchain4j.model.TokenCountEstimator;
import dev.langchain4j.model.embedding.onnx.HuggingFaceTokenCountEstimator;
import dev.langchain4j.model.openai.OpenAiChatModelName;
import dev.langchain4j.model.openai.OpenAiTokenCountEstimator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * Token 估算器工厂，根据配置选择合适实现。
 */
@Slf4j
public class TokenEstimatorFactory {

    /**
     * 创建 token 估算器。
     *
     * @param tokenEstimator 估算器名称
     * @return 估算器实现
     */
    public static TokenCountEstimator create(String tokenEstimator) {
        if (StringUtils.isBlank(tokenEstimator)) {
            // 未指定估算器时，使用默认 OpenAI 估算器保证可用性
            return new OpenAiTokenCountEstimator(OpenAiChatModelName.GPT_3_5_TURBO);
        }
        if (AdiConstant.TokenEstimator.OPENAI.equals(tokenEstimator)) {
            // 显式指定 OpenAI 时直接使用官方估算器
            return new OpenAiTokenCountEstimator(OpenAiChatModelName.GPT_3_5_TURBO);
        } else if (AdiConstant.TokenEstimator.HUGGING_FACE.equals(tokenEstimator)) {
            // HuggingFace 使用通用分词估算器
            return new HuggingFaceTokenCountEstimator();
        } else if (AdiConstant.TokenEstimator.QWEN.equals(tokenEstimator)) {
            // Qwen 优先取已注册的 DashScope 文本模型估算器
            AbstractLLMService llmService = LLMContext.getAllServices()
                    .stream()
                    .filter(item -> {
                        AiModel aiModel = item.getAiModel();
                        return aiModel.getPlatform().equals(AdiConstant.ModelPlatform.DASHSCOPE) && aiModel.getType().equals(AdiConstant.ModelType.TEXT);
                    })
                    .findFirst().orElse(null);
            if (null != llmService) {
                return llmService.getTokenEstimator();
            } else {
                // 未找到 Qwen 估算器时回退到 OpenAI 估算器
                log.warn("没有找到Qwen模型的tokenizer，使用默认的OpenAiTokenizer");
                return new OpenAiTokenCountEstimator(OpenAiChatModelName.GPT_3_5_TURBO);
            }
        }
        // 兜底使用 OpenAI 估算器，确保流程不中断
        return new OpenAiTokenCountEstimator(OpenAiChatModelName.GPT_3_5_TURBO);
    }

}
