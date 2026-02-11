package com.moyz.adi.common.rag;

import com.aliyun.core.utils.StringUtils;

/**
 * 线程级别的 token 估算器名称存取。
 */
public class TokenEstimatorThreadLocal {
    /**
     * 当前线程使用的估算器名称。
     */
    private static final ThreadLocal<String> tokenEstimator = new ThreadLocal<>();

    /**
     * 设置当前线程的估算器名称。
     *
     * @param value 估算器名称
     * @return 无
     */
    public static void setTokenEstimator(String value) {
        // 空值统一设为空字符串，避免下游 NPE
        tokenEstimator.set(StringUtils.isBlank(value) ? "" : value);
    }

    /**
     * 获取当前线程的估算器名称。
     *
     * @return 估算器名称
     */
    public static String getTokenEstimator() {
        return tokenEstimator.get();
    }

    /**
     * 清理当前线程的估算器名称。
     *
     * @return 无
     */
    public static void clearTokenEstimator() {
        // 使用 remove 释放线程变量，避免线程复用污染
        tokenEstimator.remove();
    }
}
