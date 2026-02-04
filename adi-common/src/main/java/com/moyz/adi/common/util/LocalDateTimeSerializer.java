package com.moyz.adi.common.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
/**
 * LocalDateTime 的 JSON 序列化器。
 */
public class LocalDateTimeSerializer extends JsonSerializer<LocalDateTime> {

    /**
     * 按固定格式输出 LocalDateTime。
     *
     * @param value       时间对象
     * @param gen         JSON 生成器
     * @param serializers 序列化上下文
     * @throws IOException 写入异常
     */
    @Override
    public void serialize(LocalDateTime value, JsonGenerator gen,
                          SerializerProvider serializers)
            throws IOException {
        gen.writeString(value.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }
}
