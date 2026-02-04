package com.moyz.adi.common.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * JSON 序列化与反序列化工具类。
 */
@Slf4j
public class JsonUtil {

    /**
     * 全局 ObjectMapper，统一 JSON 读写配置。
     */
    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.configure(SerializationFeature.INDENT_OUTPUT, Boolean.FALSE);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.registerModules(LocalDateTimeUtil.getSimpleModule(), new JavaTimeModule(), new Jdk8Module());
    }
    /**
     * 获取全局 ObjectMapper。
     *
     * @return ObjectMapper 实例
     */
    public static final ObjectMapper getObjectMapper() {
        return objectMapper;
    }
    /**
     * 将对象序列化为 JSON 字符串。
     *
     * @param obj 待序列化对象
     * @return JSON 字符串
     */
    public static String toJson(Object obj) {
        String resp = null;
        try {
            resp = objectMapper.writeValueAsString(obj);
        } catch (IOException e) {
            log.error("JsonUtil error", e);
        }
        return resp;
    }


    /**
     * 创建 JSON 解析器。
     *
     * @param content JSON 字符串
     * @return 解析器
     */
    private static JsonParser getParser(String content) {
        if (StringUtils.isNotBlank(content)) {
            try {
                return objectMapper.getFactory().createParser(content);
            } catch (IOException ioe) {
                log.error("JsonUtil getParser error", ioe);
            }
        }
        return null;
    }

    /**
     * 创建 JSON 生成器。
     *
     * @param sw 字符输出缓冲
     * @return 生成器
     */
    private static JsonGenerator getGenerator(StringWriter sw) {
        try {
            return objectMapper.getFactory().createGenerator(sw);
        } catch (IOException e) {
            log.error("JsonUtil getGenerator error", e);
        }
        return null;
    }

    /**
     * 将 JSON 字符串反序列化为对象。
     *
     * @param json  JSON 字符串
     * @param clazz 目标类型
     * @param <T>   目标类型
     * @return 反序列化对象
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        if (StringUtils.isBlank(json)) {
            return null;
        }
        try {
            JsonParser jp = getParser(json);
            if (null == jp) {
                log.error("json parser is null");
                return null;
            }
            return jp.readValueAs(clazz);
        } catch (IOException ioe) {
            log.error("反序列化失败", ioe);
        }
        return null;
    }
    /**
     * 将 JSON 节点反序列化为对象。
     *
     * @param jsonNode JSON 节点
     * @param clazz    目标类型
     * @param <T>      目标类型
     * @return 反序列化对象
     */
    public static <T> T fromJson(JsonNode jsonNode, Class<T> clazz) {
        try {
            return objectMapper.treeToValue(jsonNode, clazz);
        } catch (JsonProcessingException e) {
            log.error("反序列化失败", e);
        }
        return null;
    }
    /**
     * 将数组节点反序列化为列表。
     *
     * @param arrayNode 数组节点
     * @param clazz     目标类型
     * @param <T>       目标类型
     * @return 反序列化列表
     */
    public static <T> List<T> fromArrayNode(ArrayNode arrayNode, Class<T> clazz) {
        List<T> result = new ArrayList<>();
        try {
            for (JsonNode jsonNode : arrayNode) {
                result.add(objectMapper.treeToValue(jsonNode, clazz));
            }
        } catch (JsonProcessingException e) {
            log.error("反序列化失败", e);
        }
        return result;
    }
    /**
     * 将 JSON 字符串解析为 JSON 节点。
     *
     * @param json JSON 字符串
     * @return JSON 节点
     */
    public static JsonNode toJsonNode(String json) {
        try {
            return objectMapper.readTree(json);
        } catch (JsonProcessingException e) {
            log.error("反序列化失败", e);
        }
        return null;
    }
    /**
     * 将 JSON 字符串解析为 Map。
     *
     * @param json JSON 字符串
     * @return Map 结果
     */
    public static Map<String, Object> toMap(String json) {
        Map<String, Object> result;
        try {
            result = objectMapper.readValue(json, Map.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return result;
    }
    /**
     * 将 JSON 字符串解析为列表。
     *
     * @param json  JSON 字符串
     * @param clazz 元素类型
     * @param <T>   元素类型
     * @return 列表结果
     */
    public static <T> List<T> toList(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, objectMapper.getTypeFactory().constructCollectionType(List.class, clazz));
        } catch (JsonProcessingException e) {
            log.error("反序列化失败", e);
        }
        return null;
    }
    /**
     * 将对象转换为 Map。
     *
     * @param obj 对象
     * @return Map 结果
     */
    public static Map<String, Object> toMap(Object obj) {
        try {
            return objectMapper.convertValue(obj, new TypeReference<HashMap<String, Object>>() {
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    /**
     * 将对象转换为 JSON 节点。
     *
     * @param obj 对象
     * @return JSON 节点
     */
    public static JsonNode classToJsonNode(Object obj) {
        return objectMapper.valueToTree(obj);
    }
    /**
     * 创建空 ObjectNode。
     *
     * @return ObjectNode
     */
    public static ObjectNode createObjectNode() {
        return objectMapper.createObjectNode();
    }
    /**
     * 创建空 ArrayNode。
     *
     * @return ArrayNode
     */
    public static ArrayNode createArrayNode() {
        return objectMapper.createArrayNode();
    }

}
