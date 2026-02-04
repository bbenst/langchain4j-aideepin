package com.moyz.adi.common.util;
/**
 * URL 相关处理工具类。
 */
public class UrlUtil {
    /**
     * 工具类禁止实例化。
     */
    private UrlUtil() {
    }

    /**
     * 从带扩展名的字符串中提取 UUID 部分。
     *
     * @param uuidWithExt 带扩展名的 UUID
     * @return 提取后的 UUID
     */
    public static String getUuid(String uuidWithExt) {
        String tmpUuid = uuidWithExt;
        int index = uuidWithExt.indexOf(".");
        if (index > 0) {
            tmpUuid = uuidWithExt.substring(0, index);
        }
        return tmpUuid;
    }
}
