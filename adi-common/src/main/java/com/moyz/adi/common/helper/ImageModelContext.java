package com.moyz.adi.common.helper;

import com.moyz.adi.common.languagemodel.AbstractImageModelService;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

import static dev.langchain4j.model.openai.OpenAiImageModelName.DALL_E_2;

/**
 * 图片模型服务上下文（策略模式）。
 */
@Slf4j
public class ImageModelContext {

    /**
     * 图片模型服务列表。
     */
    public static final List<AbstractImageModelService> LLM_SERVICES = new ArrayList<>();
    /**
     * 私有构造函数，禁止实例化。
     */
    private ImageModelContext() {
    }
    /**
     * 新增图片模型服务。
     *
     * @param modelService 图片模型服务
     */
    public static void addImageModelService(AbstractImageModelService modelService) {
        LLM_SERVICES.add(modelService);
    }
    /**
     * 按模型名称移除服务。
     *
     * @param modelName 模型名称
     */
    public static void remove(String modelName) {
        List<AbstractImageModelService> needDeleted = LLM_SERVICES.stream()
                .filter(item -> item.getAiModel().getName().equalsIgnoreCase(modelName))
                .toList();
        LLM_SERVICES.removeAll(needDeleted);
    }
    /**
     * 清理指定平台下的服务。
     *
     * @param platform 平台名称
     */
    public static void clearByPlatform(String platform) {
        List<AbstractImageModelService> needDeleted = LLM_SERVICES.stream()
                .filter(item -> item.getAiModel().getPlatform().equalsIgnoreCase(platform))
                .toList();
        LLM_SERVICES.removeAll(needDeleted);
    }
    /**
     * 获取指定平台的首个可用模型服务。
     *
     * @param platform 平台名称
     * @return 模型服务
     */
    public static AbstractImageModelService getFirstModelService(String platform) {
        return LLM_SERVICES.stream()
                .filter(item -> item.getAiModel().getPlatform().equalsIgnoreCase(platform) && Boolean.TRUE.equals(item.getAiModel().getIsEnable()))
                .findFirst().orElse(null);
    }
    /**
     * 获取模型服务，找不到时使用默认模型。
     *
     * @param modelName 模型名称
     * @return 模型服务
     */
    public static AbstractImageModelService getOrDefault(String modelName) {
        return getBy(modelName, true);
    }
    /**
     * 按模型名称获取服务。
     *
     * @param modelName 模型名称
     * @param useDefault 是否使用默认模型
     * @return 模型服务
     */
    public static AbstractImageModelService getBy(String modelName, boolean useDefault) {
        AbstractImageModelService service = LLM_SERVICES.stream().filter(item -> item.getAiModel().getName().equalsIgnoreCase(modelName)).findFirst().orElse(null);
        if (null == service && useDefault) {
            log.warn("︿︿︿ Can not find {}, use the default model DALL_E_2 ︿︿︿", modelName);
            return getByModelName(DALL_E_2.toString());
        }
        return service;
    }
    /**
     * 按模型名称获取服务。
     *
     * @param modelName 模型名称
     * @return 模型服务
     */
    private static AbstractImageModelService getByModelName(String modelName) {
        return LLM_SERVICES.stream().filter(item -> item.getAiModel().getName().equalsIgnoreCase(modelName)).findFirst().orElse(null);
    }
}
