package com.moyz.adi.common.util;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
/**
 * Spring 容器工具类。
 */
@Component
public class SpringUtil implements ApplicationContextAware {
    /**
     * Spring 应用上下文。
     */
    private static ApplicationContext applicationContext;
    /**
     * 设置 Spring 应用上下文。
     *
     * @param applicationContext Spring 应用上下文
     * @throws BeansException Bean 异常
     */
    @Override
    public void setApplicationContext(@NotNull ApplicationContext applicationContext) throws BeansException {
        SpringUtil.applicationContext = applicationContext;
    }
    /**
     * 根据名称和类型获取 Bean。
     *
     * @param name  Bean 名称
     * @param clazz Bean 类型
     * @param <T>   类型
     * @return Bean 实例
     */
    public static <T> T getBean(String name, Class<T> clazz) {
        return applicationContext.getBean(name, clazz);
    }
    /**
     * 根据类型获取 Bean。
     *
     * @param clazz Bean 类型
     * @param <T>   类型
     * @return Bean 实例
     */
    public static <T> T getBean(Class<T> clazz) {
        return applicationContext.getBean(clazz);
    }
    /**
     * 获取配置属性值。
     *
     * @param key 配置键
     * @return 配置值
     */
    public static String getProperty(String key) {
        return applicationContext.getEnvironment().getProperty(key);
    }
}
