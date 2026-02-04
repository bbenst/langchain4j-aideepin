package com.moyz.adi.common.util;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.apache.commons.lang3.StringUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
/**
 * 本地时间处理工具类。
 */
public class LocalDateTimeUtil {

    /**
     * 默认时间格式：yyyy-MM-dd HH:mm:ss。
     */
    public static final DateTimeFormatter PATTERN_DEFAULT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    /**
     * 时间格式：yyyyMMddmmHHss。
     */
    public static final DateTimeFormatter PATTERN_YYYYMMDDMMHHSS = DateTimeFormatter.ofPattern("yyyyMMddmmHHss");
    /**
     * 日期格式：yyyy-MM-dd。
     */
    public static final DateTimeFormatter PATTERN_YYYY_MM_DD = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    /**
     * 工具类禁止实例化。
     */
    private LocalDateTimeUtil() {
    }
    /**
     * 获取 Jackson 时间序列化/反序列化模块。
     *
     * @return SimpleModule
     */
    public static SimpleModule getSimpleModule() {
        // jackson中自定义处理序列化和反序列化
        SimpleModule customModule = new SimpleModule();
        customModule.addSerializer(Long.class, ToStringSerializer.instance);
        // 时间序列化
        customModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer());
        customModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer());
        return customModule;
    }
    /**
     * 按默认格式解析时间字符串。
     *
     * @param localDateTime 时间字符串
     * @return 时间对象
     */
    public static LocalDateTime parse(String localDateTime) {
        return LocalDateTime.parse(localDateTime, PATTERN_DEFAULT);
    }
    /**
     * 将毫秒时间戳解析为本地时间。
     *
     * @param epochMilli 毫秒时间戳
     * @return 时间对象
     */
    public static LocalDateTime parse(Long epochMilli) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMilli), ZoneId.systemDefault());
    }
    /**
     * 按默认格式输出时间字符串。
     *
     * @param localDateTime 时间对象
     * @return 时间字符串
     */
    public static String format(LocalDateTime localDateTime) {
        if (null == localDateTime) {
            return StringUtils.EMPTY;
        }
        return localDateTime.format(PATTERN_DEFAULT);
    }
    /**
     * 按指定格式输出时间字符串。
     *
     * @param localDateTime 时间对象
     * @param pattern       格式字符串
     * @return 时间字符串
     */
    public static String format(LocalDateTime localDateTime, String pattern) {
        if (null == localDateTime) {
            return StringUtils.EMPTY;
        }
        return localDateTime.format(DateTimeFormatter.ofPattern(pattern));
    }
    /**
     * 按指定格式输出时间字符串。
     *
     * @param localDateTime 时间对象
     * @param pattern       格式化器
     * @return 时间字符串
     */
    public static String format(LocalDateTime localDateTime, DateTimeFormatter pattern) {
        if (null == localDateTime) {
            return StringUtils.EMPTY;
        }
        return localDateTime.format(pattern);
    }
    /**
     * 将日期转换为整型格式：yyyyMMdd。
     *
     * @param localDateTime 时间对象
     * @return 整型日期
     */
    public static int getIntDay(LocalDateTime localDateTime) {
        return localDateTime.getYear() * 10000 + localDateTime.getMonthValue() * 100 + localDateTime.getDayOfMonth();
    }
    /**
     * 获取当天日期的整型格式：yyyyMMdd。
     *
     * @return 整型日期
     */
    public static int getToday() {
        LocalDateTime now = LocalDateTime.now();
        return now.getYear() * 10000 + now.getMonthValue() * 100 + now.getDayOfMonth();
    }
}
