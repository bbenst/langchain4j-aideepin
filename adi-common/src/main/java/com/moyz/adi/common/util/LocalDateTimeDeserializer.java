package com.moyz.adi.common.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
/**
 * LocalDateTime 的 JSON 反序列化器。
 */
public class LocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {

    /**
     * 按固定格式解析 LocalDateTime。
     *
     * @param p                       JSON 解析器
     * @param deserializationContext  反序列化上下文
     * @return 时间对象
     * @throws IOException 解析异常
     */
    @Override
    public LocalDateTime deserialize(JsonParser p,
                                     DeserializationContext deserializationContext)
            throws IOException {
        return LocalDateTime.parse(p.getValueAsString(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
