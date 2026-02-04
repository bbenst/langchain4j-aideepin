package com.moyz.adi.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 分布式锁注解。
 *
 * @author moyz
 * date:2021-07-15
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface DistributeLock {

    /**
     * Redis 键。
     *
     * @return Redis 键
     */
    String redisKey() default "";

    /**
     * 客户端标识，用于区分加锁方。
     *
     * @return 客户端标识
     */
    String clientId() default "";

    /**
     * 失效时间（秒）。
     *
     * @return 失效时间
     */
    int expireInSeconds() default 0;

    /**
     * 获取锁失败时是否继续执行后续逻辑。
     *
     * @return 是否继续执行
     */
    boolean continueIfAcquireFail() default true;

}
