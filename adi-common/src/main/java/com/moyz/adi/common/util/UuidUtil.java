package com.moyz.adi.common.util;

import java.util.UUID;
/**
 * UUID 生成工具类。
 */
public class UuidUtil {
    /**
     * 工具类禁止实例化。
     */
    private UuidUtil(){}
    /**
     * 创建不含分隔符的 UUID。
     *
     * @return UUID 字符串
     */
    public static String createShort() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
