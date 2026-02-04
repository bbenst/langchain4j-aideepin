package com.moyz.adi.common.util;

import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
/**
 * Redis 分布式锁工具类。
 */
@Component
public class RedisTemplateUtil {
    /**
     * Redis 操作模板。
     */
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    /**
     * 获取分布式锁。
     *
     * @param key               锁键
     * @param clientId          客户端标识
     * @param lockExpireInSecond 锁超时秒数
     * @return 是否加锁成功
     */
    public boolean lock(String key, String clientId, int lockExpireInSecond) {
        return Boolean.TRUE.equals(stringRedisTemplate.opsForValue().setIfAbsent(key, clientId, lockExpireInSecond, TimeUnit.SECONDS));
    }
    /**
     * 释放分布式锁。
     *
     * @param key      锁键
     * @param clientId 客户端标识
     * @return 是否释放成功
     */
    public boolean unlock(String key, String clientId) {
        boolean result = false;
        if (clientId.equals(stringRedisTemplate.opsForValue().get(key))) {
            result = Boolean.TRUE.equals(stringRedisTemplate.delete(key));
        }
        return result;
    }

}
