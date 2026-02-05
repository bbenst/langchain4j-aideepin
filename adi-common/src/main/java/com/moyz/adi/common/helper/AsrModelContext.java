package com.moyz.adi.common.helper;

import com.fasterxml.jackson.databind.JsonNode;
import com.moyz.adi.common.cosntant.AdiConstant;
import com.moyz.adi.common.enums.ErrorEnum;
import com.moyz.adi.common.exception.BaseException;
import com.moyz.adi.common.service.SysConfigService;
import com.moyz.adi.common.languagemodel.AbstractAsrModelService;
import com.moyz.adi.common.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 语音识别（ASR）模型上下文。
 */
@Slf4j
public class AsrModelContext {

    /**
     * 模型名称到 ASR 服务的映射。
     */
    private static final Map<String, AbstractAsrModelService> NAME_TO_SERVICE = new HashMap<>();
    /**
     * 当前使用的 ASR 服务。
     */
    private final AbstractAsrModelService current;

    /**
     * 直接由系统设置来决定使用哪个ASR模型，不需要让用户选择。
     *
     * @throws BaseException ASR 配置缺失或模型不可用时抛出异常
     */
    public AsrModelContext() {
        String asrSetting = SysConfigService.getByKey(AdiConstant.SysConfigKey.ASR_SETTING);
        log.info("asr model setting:{}", asrSetting);
        JsonNode jsonNode = JsonUtil.toJsonNode(asrSetting);
        if (null != jsonNode) {
            String modelName = jsonNode.get("model_name").asText();
            this.current = NAME_TO_SERVICE.get(modelName);
            if (null == this.current) {
                log.error("asr model not found,modelName:{}", modelName);
                throw new BaseException(ErrorEnum.B_ASR_MODEL_NOT_FOUND);
            }
        } else {
            throw new BaseException(ErrorEnum.B_ASR_SETTING_NOT_FOUND);
        }
    }
    /**
     * 将音频转换为文本。
     *
     * @param urlOrUuid 音频地址或 UUID
     * @return 识别文本
     * @throws BaseException ASR 服务不可用或识别失败时抛出异常
     */
    public String audioToText(String urlOrUuid) {
        return this.current.audioToText(urlOrUuid);
    }
    /**
     * 注册 ASR 服务。
     *
     * @param modelService ASR 服务
     * @return 无
     */
    public static void addService(AbstractAsrModelService modelService) {
        NAME_TO_SERVICE.put(modelService.getAiModel().getName(), modelService);
    }
    /**
     * 清理指定平台下的 ASR 服务。
     *
     * @param platform 平台名称
     * @return 无
     */
    public static void clearByPlatform(String platform) {
        List<String> needDeleted = NAME_TO_SERVICE.values()
                .stream()
                .filter(item -> item.getAiModel().getPlatform().equalsIgnoreCase(platform))
                .map(item -> item.getAiModel().getName())
                .toList();
        for (String key : needDeleted) {
            log.info("delete asr model service,modelName:{}", key);
            NAME_TO_SERVICE.remove(key);
        }
    }
}
