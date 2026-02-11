package com.moyz.adi.common.helper;

import com.fasterxml.jackson.databind.JsonNode;
import com.moyz.adi.common.cosntant.AdiConstant;
import com.moyz.adi.common.enums.ErrorEnum;
import com.moyz.adi.common.exception.BaseException;
import com.moyz.adi.common.service.SysConfigService;
import com.moyz.adi.common.languagemodel.AbstractTtsModelService;
import com.moyz.adi.common.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
/**
 * 语音合成模型上下文。
 */
@Slf4j
public class TtsModelContext {

    /**
     * 模型名称到TTS服务的映射表。
     */
    private static final Map<String, AbstractTtsModelService> NAME_TO_SERVICE = new HashMap<>();
    /**
     * 当前生效的TTS模型服务。
     */
    private final AbstractTtsModelService current;

    /**
     * 直接由系统设置来决定使用哪个 TTS 模型，不需要让用户选择。
     *
     * @throws BaseException 配置缺失或模型未找到时抛出异常
     */
    public TtsModelContext() {
        // 从系统配置读取 TTS 设置，确保模型选择与后台一致
        String asrSetting = SysConfigService.getByKey(AdiConstant.SysConfigKey.TTS_SETTING);
        log.info("tts model setting:{}", asrSetting);
        JsonNode jsonNode = JsonUtil.toJsonNode(asrSetting);
        if (null != jsonNode) {
            // 按配置的模型名称定位具体实现
            String modelName = jsonNode.get("model_name").asText();
            this.current = NAME_TO_SERVICE.get(modelName);
            if (null == this.current) {
                // 配置存在但未注册模型时直接失败，避免后续空指针
                log.error("asr model not found,modelName:{}", modelName);
                throw new BaseException(ErrorEnum.B_TTS_MODEL_NOT_FOUND);
            }
        } else {
            // 配置缺失时直接抛错，避免进入不完整流程
            throw new BaseException(ErrorEnum.B_TTS_SETTING_NOT_FOUND);
        }
    }
    /**
     * 注册TTS模型服务。
     *
     * @param modelService 模型服务实现
     * @return 无
     */
    public static void addService(AbstractTtsModelService modelService) {
        // 按模型名称缓存，便于按配置快速定位
        NAME_TO_SERVICE.put(modelService.getAiModel().getName(), modelService);
    }
    /**
     * 按平台清理对应的模型服务。
     *
     * @param platform 平台标识
     * @return 无
     */
    public static void clearByPlatform(String platform) {
        // 先筛出要删除的模型名称，避免遍历时修改集合
        List<String> needDeleted = NAME_TO_SERVICE.values()
                .stream()
                .filter(item -> item.getAiModel().getPlatform().equalsIgnoreCase(platform))
                .map(item -> item.getAiModel().getName())
                .toList();
        for (String key : needDeleted) {
            log.info("delete tts model service,modelName:{}", key);
            NAME_TO_SERVICE.remove(key);
        }
    }

    /**
     * 开启一个TTS任务
     *
     * @param jobId      任务ID
     * @param voice      声音
     * @param onProcess  处理回调
     * @param onComplete 完成回调
     * @param onError    异常回调
     * @return 无
     */
    public void startTtsJob(String jobId, String voice, Consumer<ByteBuffer> onProcess, Consumer<String> onComplete, Consumer<String> onError) {
        log.info("start tts job,jobId:{},voice:{}", jobId, voice);
        // 委托给当前选中的 TTS 服务实现
        current.start(jobId, voice, onProcess, onComplete, onError);
    }

    /**
     * 处理文本
     *
     * @param jobId 任务id
     * @param text  文本内容
     * @return 无
     */
    public void processPartialText(String jobId, String text) {
        // 流式写入文本，触发 TTS 增量合成
        current.processByStream(jobId, text);
    }

    /**
     * 主动完成TTS任务，会触发onComplete回调
     *
     * @param jobId 任务id
     * @return 无
     */
    public void complete(String jobId) {
        // 结束合成流程并触发完成回调
        current.complete(jobId);
    }

}
