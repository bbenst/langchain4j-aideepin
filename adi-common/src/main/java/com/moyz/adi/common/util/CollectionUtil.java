package com.moyz.adi.common.util;

import org.apache.commons.lang3.SerializationUtils;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;
/**
 * 集合工具类。
 */
public class CollectionUtil {
    /**
     * 工具类禁止实例化。
     */
    private CollectionUtil() {
    }

    /**
     * 对列表进行深拷贝。
     *
     * @param source 原列表
     * @param <T>    元素类型
     * @return 深拷贝后的列表
     */
    public static <T extends Serializable> List<T> deepCopy(List<T> source) {
        return source.stream().map(SerializationUtils::clone).collect(Collectors.toList());
    }
}
