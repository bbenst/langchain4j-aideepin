package com.moyz.adi.common.helper;

import com.aliyun.core.utils.StringUtils;
import com.moyz.adi.common.entity.AiModel;
import com.moyz.adi.common.enums.ErrorEnum;
import com.moyz.adi.common.exception.BaseException;
import com.moyz.adi.common.languagemodel.AbstractLLMService;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 大模型服务上下文（策略模式）。
 */
@Slf4j
public class LLMContext {

    /**
     * 已注册的大模型服务列表。
     */
    protected static final List<AbstractLLMService> LLM_SERVICES = new ArrayList<>();
    /**
     * 私有构造函数，禁止实例化。
     */
    private LLMContext() {
    }
    /**
     * 注册大模型服务。
     *
     * @param llmService 大模型服务
     * @return 无
     */
    public static void addLLMService(AbstractLLMService llmService) {
        LLM_SERVICES.add(llmService);
    }

    /**
     * 清除指定平台与模型类型下的服务缓存。
     *
     * @param modelPlatform 模型所属的平台
     * @param modelType 模型类型
     * @return 无
     */
    public static void clearByPlatform(String modelPlatform, String modelType) {
        List<AbstractLLMService> readyToDelete = LLM_SERVICES.stream()
                .filter(item -> item.getAiModel().getPlatform().equalsIgnoreCase(modelPlatform) && item.getAiModel().getType().equalsIgnoreCase(modelType))
                .toList();
        for (AbstractLLMService key : readyToDelete) {
            log.info("delete llm model service,modelName:{}", key.getAiModel().getName());
            LLM_SERVICES.remove(key);
        }
    }
    /**
     * 移除指定平台与模型名称的服务。
     *
     * @param platform 平台名称
     * @param modelName 模型名称
     * @return 无
     */
    public static void remove(String platform, String modelName) {
        List<AbstractLLMService> readyToDelete = LLM_SERVICES.stream()
                .filter(item -> item.getPlatform().getName().equals(platform) && item.getAiModel().getName().equalsIgnoreCase(modelName))
                .toList();
        for (AbstractLLMService key : readyToDelete) {
            log.info("delete llm model service,modelName:{}", key.getAiModel().getName());
            LLM_SERVICES.remove(key);
        }
    }

    /**
     * 以 {platform} 和 {modelName} 定位一个 AiModel
     * 不能只使用 {modelName} 来获取 AiModel，以防不同平台有相同名字的模型
     *
     * @param platform  模型平台名称
     * @param modelName 模型名称
     * @return AiModel
     */
    public static AiModel getAiModel(String platform, String modelName) {
        return LLM_SERVICES.stream()
                // 兼容只传 modelName 的情况
                .filter(item -> (StringUtils.isBlank(platform) || item.getPlatform().getName().equals(platform)) && item.getAiModel().getName().equalsIgnoreCase(modelName))
                .findFirst()
                .map(AbstractLLMService::getAiModel)
                .orElse(null);
    }
    /**
     * 获取全部服务列表。
     *
     * @return 服务列表
     */
    public static List<AbstractLLMService> getAllServices() {
        return LLM_SERVICES;
    }
    /**
     * 获取服务，若不存在则使用默认模型。
     *
     * @param platform 平台名称
     * @param modelName 模型名称
     * @return 大模型服务
     * @throws BaseException 当无法找到可用模型时抛出异常
     */
    public static AbstractLLMService getServiceOrDefault(String platform, String modelName) {
        return getServiceByPlatformAndModel(platform, modelName, true);
    }
    /**
     * 按模型 ID 获取服务。
     *
     * @param modelId 模型 ID
     * @param useDefault 是否使用默认模型
     * @return 大模型服务
     * @throws BaseException 当无法找到可用模型时抛出异常
     */
    public static AbstractLLMService getServiceById(Long modelId, boolean useDefault) {
        AbstractLLMService service = LLM_SERVICES.stream()
                .filter(item -> item.getAiModel().getId().equals(modelId))
                .findFirst()
                .orElse(null);
        if (null == service && useDefault) {
            // 找不到指定模型时，按策略回退到第一个可用的免费模型
            Optional<AbstractLLMService> serviceOptional = getFirstEnableAndFree();
            if (serviceOptional.isPresent()) {
                log.warn("︿︿︿ 找不到 modelId:{},使用第1个可用的免费模型 {} ︿︿︿", modelId, serviceOptional.get().getAiModel().getName());
                return serviceOptional.get();
            }
            log.error("︿︿︿ 没有可用的模型,请检查平台及模型的配置 ︿︿︿");
            throw new BaseException(ErrorEnum.A_ENABLE_MODEL_NOT_FOUND);
        }
        return service;
    }

    /**
     * 以 {platform} 和 {modelName} 查找 AbstractLLMService，如果找不到，则返回第1个可用的免费模型
     * PS: 不能只使用 {modelName} 来获取 AbstractLLMService，以防不同平台有相同名字的模型
     *
     * @param platform   模型平台名称
     * @param modelName  模型名称
     * @param useDefault 如果找不到，是否使用第1个可用的免费模型
     * @return AbstractLLMService
     * @throws BaseException 当无法找到可用模型时抛出异常
     */
    public static AbstractLLMService getServiceByPlatformAndModel(String platform, String modelName, boolean useDefault) {
        AbstractLLMService service = LLM_SERVICES.stream()
                // 兼容只传 modelName 的情况
                .filter(item -> (StringUtils.isBlank(platform) || item.getPlatform().getName().equals(platform)) && item.getAiModel().getName().equalsIgnoreCase(modelName))
                .findFirst()
                .orElse(null);
        if (null == service && useDefault) {
            Optional<AbstractLLMService> serviceOptional = getFirstEnableAndFree();
            if (serviceOptional.isPresent()) {
                log.warn("︿︿︿ 找不到 {},使用第1个可用的免费模型 {} ︿︿︿", modelName, serviceOptional.get().getAiModel().getName());
                return serviceOptional.get();
            }
            log.error("︿︿︿ 没有可用的模型,请检查平台及模型的配置 ︿︿︿");
            throw new BaseException(ErrorEnum.A_ENABLE_MODEL_NOT_FOUND);
        }
        return service;
    }

    /**
     * 选择顺序：
     * 一、优先选择免费可用的模型；
     * 二、收费可用的模型
     *
     * @return 返回免费或收费的可用模型
     */
    public static Optional<AbstractLLMService> getFirstEnableAndFree() {
        AbstractLLMService freeObj = null;
        AbstractLLMService enableObj = null;
        for (AbstractLLMService service : LLM_SERVICES) {
            AiModel aiModel = service.getAiModel();
            if (aiModel.getIsEnable() && aiModel.getIsFree()) {
                freeObj = service;
                break;
            } else if (null == enableObj && Boolean.TRUE.equals(aiModel.getIsEnable())) {
                enableObj = service;
            }
        }
        if (null != freeObj) {
            return Optional.of(freeObj);
        } else if (null != enableObj) {
            return Optional.of(enableObj);
        }
        return Optional.empty();
    }
}
