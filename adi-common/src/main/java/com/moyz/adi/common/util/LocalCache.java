package com.moyz.adi.common.util;

import com.moyz.adi.common.entity.AiModel;
import com.moyz.adi.common.vo.RequestRateLimit;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
/**
 * 应用本地缓存容器。
 */
public class LocalCache {
    /**
     * 配置项缓存（key/value）。
     */
    public static final Map<String, String> CONFIGS = new ConcurrentHashMap<>();
    /**
     * 文本请求限流配置。
     */
    public static RequestRateLimit TEXT_RATE_LIMIT_CONFIG;
    /**
     * 图片请求限流配置。
     */
    public static RequestRateLimit IMAGE_RATE_LIMIT_CONFIG;

    /**
     * 模型 ID 到模型对象的缓存。
     */
    public static Map<Long, AiModel> MODEL_ID_TO_OBJ = new ConcurrentHashMap<>();
}
