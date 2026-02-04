package com.moyz.adi.common.util;

import com.moyz.adi.common.languagemodel.data.LLMException;
import com.theokanning.openai.OpenAiError;
import com.theokanning.openai.OpenAiHttpException;
/**
 * OpenAI 异常解析工具类。
 */
public class OpenAiUtil {

    /**
     * 工具类禁止实例化。
     */
    private OpenAiUtil(){}

    /**
     * 解析 OpenAI 错误并转换为统一异常对象。
     * openai 错误格式示例：
     * OpenAiHttpException: {
     *   "error": {
     *     "code": "content_policy_violation",
     *     "message": "Your request was rejected as a result of our safety system. Your prompt may contain text that is not allowed by our safety system.",
     *     "param": null,
     *     "type": "invalid_request_error"
     *   }
     * }
     *
     * @param error 异常对象
     * @return 统一异常信息
     */
    public static LLMException parseError(Object error) {
        if (error instanceof OpenAiHttpException openAiHttpException) {
            OpenAiError openAiError = JsonUtil.fromJson(openAiHttpException.getMessage(), OpenAiError.class);
            if (null != openAiError) {
                OpenAiError.OpenAiErrorDetails errorDetails = openAiError.getError();
                LLMException llmException = new LLMException();
                llmException.setType(errorDetails.getType());
                llmException.setCode(errorDetails.getCode());
                llmException.setMessage(errorDetails.getMessage());
                return llmException;
            }
        }
        return null;
    }
}
