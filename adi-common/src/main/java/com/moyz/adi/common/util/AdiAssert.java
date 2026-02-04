package com.moyz.adi.common.util;

import java.util.Collection;

/**
 * 断言工具类。
 */
public class AdiAssert {

    /**
     * 私有构造函数，禁止实例化。
     */
    private AdiAssert() {
        throw new AssertionError("Cannot instantiate AdiAssert class.");
    }

    /**
     * 断言条件为真，否则抛出 IllegalArgumentException。
     *
     * @param condition 条件
     * @param message 异常信息
     */
    public static void isTrue(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * 断言条件为假，否则抛出 IllegalArgumentException。
     *
     * @param condition 条件
     * @param message 异常信息
     */
    public static void isFalse(boolean condition, String message) {
        if (condition) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * 断言对象非空，否则抛出 NullPointerException。
     *
     * @param object 对象
     * @param message 异常信息
     */
    public static void isNotNull(Object object, String message) {
        if (object == null) {
            throw new NullPointerException(message);
        }
    }

    /**
     * 断言字符串非空且非空串，否则抛出 IllegalArgumentException。
     *
     * @param str 字符串
     * @param message 异常信息
     */
    public static void isNotEmpty(String str, String message) {
        if (str == null || str.isEmpty()) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * 断言集合非空且非空集合，否则抛出 IllegalArgumentException。
     *
     * @param collection 集合
     * @param message 异常信息
     * @param <T> 元素类型
     */
    public static <T> void isNotEmpty(Collection<T> collection, String message) {
        if (collection == null || collection.isEmpty()) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * 断言两个对象相等，否则抛出 IllegalArgumentException。
     *
     * @param expected 期望值
     * @param actual 实际值
     * @param message 异常信息
     */
    public static void isEquals(Object expected, Object actual, String message) {
        if (!expected.equals(actual)) {
            throw new IllegalArgumentException(message + " Expected: " + expected + ", but was: " + actual);
        }
    }

    /**
     * 断言数组长度符合预期，否则抛出 IllegalArgumentException。
     *
     * @param array 数组
     * @param length 期望长度
     * @param message 异常信息
     */
    public static void isArrayLength(Object[] array, int length, String message) {
        if (array == null || array.length != length) {
            throw new IllegalArgumentException(message + " Expected length: " + length + ", but was: " + (array == null ? "null" : array.length));
        }
    }

    /**
     * 断言数值在区间内（含边界），否则抛出 IllegalArgumentException。
     *
     * @param value 数值
     * @param min 最小值
     * @param max 最大值
     * @param message 异常信息
     */
    public static void isInRange(int value, int min, int max, String message) {
        if (value < min || value > max) {
            throw new IllegalArgumentException(message + " Value: " + value + " is out of range [" + min + ", " + max + "].");
        }
    }
}
