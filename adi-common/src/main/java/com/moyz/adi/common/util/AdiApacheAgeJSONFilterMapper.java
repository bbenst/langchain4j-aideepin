package com.moyz.adi.common.util;

import org.apache.commons.lang3.StringUtils;
/**
 * Apache AGE JSON 过滤条件映射器。
 */
public class AdiApacheAgeJSONFilterMapper extends AdiApacheAgeFilterMapper {
    /**
     * 元数据字段名。
     */
    final String metadataColumn;
    /**
     * 创建 JSON 过滤条件映射器。
     *
     * @param metadataColumn 元数据列名
     */
    public AdiApacheAgeJSONFilterMapper(String metadataColumn) {
        this.metadataColumn = metadataColumn;
    }

    /**
     * 格式化 JSON 字段访问表达式。
     *
     * @param key       字段名
     * @param valueType 值类型
     * @return 拼接后的字段路径
     */
    String formatKey(String key, Class<?> valueType) {
        String metadataName = metadataColumn;
        if (StringUtils.isNotBlank(alias)) {
            metadataName = alias + "." + metadataName;
        }
        return metadataName + "." + key;
    }

    /**
     * 格式化 JSON 字段访问表达式（字符串形式）。
     *
     * @param key 字段名
     * @return 拼接后的字段路径
     */
    String formatKeyAsString(String key) {
        String metadataName = metadataColumn;
        if (StringUtils.isNotBlank(alias)) {
            metadataName = alias + "." + metadataName;
        }
        return metadataName + "." + key;
    }


    /**
     * 格式化 JSON 字段访问表达式（JSON 字符串形式）。
     *
     * @param key 字段名
     * @return 拼接后的字段路径
     */
    String formatJsonKeyAsString(String key) {
        String metadataName = metadataColumn;
        if (StringUtils.isNotBlank(alias)) {
            metadataName = alias + "." + metadataName;
        }
        return metadataName + "." + key;
    }
}
