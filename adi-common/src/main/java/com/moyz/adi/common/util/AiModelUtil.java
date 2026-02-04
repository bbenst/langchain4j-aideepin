package com.moyz.adi.common.util;

import com.moyz.adi.common.cosntant.AdiConstant;
import org.apache.commons.lang3.StringUtils;
/**
 * AI 模型校验工具类。
 */
public class AiModelUtil {
    /**
     * 工具类禁止实例化。
     */
    private AiModelUtil() {
    }
    /**
     * 校验模型类型是否有效。
     *
     * @param modelType 模型类型
     * @return 是否有效
     */
    public static boolean checkModelType(String modelType) {
        return AdiConstant.ModelType.getModelType().contains(modelType);
    }
    /**
     * 校验模型平台是否有效。
     *
     * @param platform 平台标识
     * @return 是否有效
     */
    public static boolean checkModelPlatform(String platform) {
        return AdiConstant.ModelPlatform.getModelConstants().contains(platform);
    }
}
