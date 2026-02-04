package com.moyz.adi.chat.controller;

import com.moyz.adi.common.entity.AiModel;
import com.moyz.adi.common.entity.ModelPlatform;
import com.moyz.adi.common.helper.ImageModelContext;
import com.moyz.adi.common.helper.LLMContext;
import com.moyz.adi.common.service.ModelPlatformService;
import com.moyz.adi.common.languagemodel.data.ImageModelInfo;
import com.moyz.adi.common.languagemodel.data.LLMModelInfo;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
/**
 * 模型与平台信息查询接口控制器。
 */
@RestController
@RequestMapping("/model")
public class ModelController {

    /**
     * 模型平台服务，提供平台信息查询。
     */
    @Resource
    private ModelPlatformService modelPlatformService;

    /**
     * 查询支持的大语言模型列表。
     *
     * @return 大语言模型列表
     */
    @Operation(summary = "支持的大语言模型列表")
    @GetMapping(value = "/llms")
    public List<LLMModelInfo> llms() {
        return LLMContext.getAllServices().stream().map(item -> {
            AiModel aiModel = item.getAiModel();
            LLMModelInfo modelInfo = new LLMModelInfo();
            modelInfo.setModelId(aiModel.getId());
            modelInfo.setModelName(aiModel.getName());
            modelInfo.setModelTitle(aiModel.getTitle());
            modelInfo.setModelPlatform(aiModel.getPlatform());
            modelInfo.setEnable(aiModel.getIsEnable());
            BeanUtils.copyProperties(aiModel, modelInfo);
            return modelInfo;
        }).toList();
    }

    /**
     * 查询支持的图片模型列表。
     *
     * @return 图片模型列表
     */
    @Operation(summary = "支持的图片模型列表")
    @GetMapping(value = "/imageModels")
    public List<ImageModelInfo> imageModels() {
        return ImageModelContext.LLM_SERVICES.stream().map(item -> {
            AiModel aiModel = item.getAiModel();
            ImageModelInfo modelInfo = new ImageModelInfo();
            modelInfo.setModelId(aiModel.getId());
            modelInfo.setModelName(aiModel.getName());
            modelInfo.setModelTitle(aiModel.getTitle());
            modelInfo.setModelPlatform(aiModel.getPlatform());
            modelInfo.setEnable(aiModel.getIsEnable());
            BeanUtils.copyProperties(aiModel, modelInfo);
            return modelInfo;
        }).toList();
    }

    /**
     * 查询模型平台列表，并清理敏感字段。
     *
     * @return 模型平台列表
     */
    @Operation(summary = "模型平台列表")
    @GetMapping(value = "/platforms")
    public List<ModelPlatform> platforms() {
        List<ModelPlatform> platforms = modelPlatformService.listAll();
        platforms.forEach(item -> {
            // 出参不返回敏感信息，避免泄露密钥
            item.setApiKey("");
            item.setSecretKey("");
        });
        return platforms;
    }
}
