package com.moyz.adi.common.aop;

import com.moyz.adi.common.annotation.DistributeLock;
import com.moyz.adi.common.util.RedisTemplateUtil;
import com.moyz.adi.common.util.UuidUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.UUID;
/**
 * 分布式锁切面，基于注解实现方法级别加锁。
 *
 * @author moyz
 */
@Slf4j
@Aspect
@Component
public class DistributeLockAspect {

    /**
     * Redis 操作工具，用于加锁与解锁。
     */
    @Resource
    private RedisTemplateUtil redisTemplateUtil;

    /**
     * 执行方法前尝试加锁，执行后释放锁。
     *
     * @param joinPoint 连接点
     * @param distributeLock 分布式锁注解
     * @return 方法执行结果
     * @throws Throwable 方法执行异常
     */
    @Around("@annotation(distributeLock)")
    public Object around(ProceedingJoinPoint joinPoint, DistributeLock distributeLock) throws Throwable {
        String key = distributeLock.redisKey();
        int expireInSeconds = distributeLock.expireInSeconds();
        boolean continueIfAcquireFail = distributeLock.continueIfAcquireFail();
        String clientId = distributeLock.clientId();
        boolean lockAndContinue = checkAndLock(key, clientId, expireInSeconds, continueIfAcquireFail, redisTemplateUtil);
        if (!lockAndContinue) {
            log.warn("该次请求忽略");
            return false;
        }
        try {
            return joinPoint.proceed();
        } finally {
            boolean unlockResult = redisTemplateUtil.unlock(key, clientId);
            log.info("unlock:{},key:{},clientId:{}", unlockResult, key, clientId);
        }
    }

    /**
     * 校验参数并加锁，未传客户端标识时自动生成。
     *
     * @param key 锁键
     * @param clientId              加锁方标识
     * @param expireInSeconds       超时时间 （秒）
     * @param continueIfAcquireFail 获取锁失败是否继续执行后面的业务逻辑
     * @param redisTemplateUtil     Redis 工具类
     * @return 是否加锁成功或允许继续执行
     * @throws Exception 参数不合法时抛出异常
     */
    public static boolean checkAndLock(String key, String clientId, int expireInSeconds, boolean continueIfAcquireFail, RedisTemplateUtil redisTemplateUtil) throws Exception {
        log.info("lock info,key:{},clientId:{},expireInSecond:{},continueIfAcquireFail:{}", key, clientId, expireInSeconds, continueIfAcquireFail);
        if (StringUtils.isBlank(key) || expireInSeconds < 1) {
            log.warn("加锁参数有误，请确认后再操作");
            throw new Exception("加锁参数有误，请确认后再操作");
        }
        if (StringUtils.isBlank(clientId)) {
            clientId = UuidUtil.createShort();
        }
        boolean lock = redisTemplateUtil.lock(key, clientId, expireInSeconds);
        if (!lock && !continueIfAcquireFail) {
            log.warn("由于参数continueIfAcquireFail为false并且获取锁失败，此次请求忽略");
            return false;
        }
        return lock;
    }
}
