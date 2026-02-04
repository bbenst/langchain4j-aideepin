package com.moyz.adi.common.helper;

import com.moyz.adi.common.vo.RequestRateLimit;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
/**
 * 频率限制辅助类。
 */
@Service
public class RateLimitHelper {
    /**
     * Redis 操作模板。
     */
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 按固定时间窗口判断是否超限。
     *
     * @param requestTimesKey Redis 键
     * @param rateLimitConfig 请求频率限制配置
     * @return 是否允许继续请求
     */
    public boolean checkRequestTimes(String requestTimesKey, RequestRateLimit rateLimitConfig) {
        int requestCountInTimeWindow = 0;
        String rateLimitVal = stringRedisTemplate.opsForValue().get(requestTimesKey);
        if (StringUtils.isNotBlank(rateLimitVal)) {
            requestCountInTimeWindow = Integer.parseInt(rateLimitVal);
        }
        return requestCountInTimeWindow < rateLimitConfig.getTimes();
    }
    /**
     * 增加请求次数计数。
     *
     * @param requestTimesKey Redis 键
     * @param rateLimitConfig 请求频率限制配置
     */
    public void increaseRequestTimes(String requestTimesKey, RequestRateLimit rateLimitConfig) {
        long expireTime = stringRedisTemplate.getExpire(requestTimesKey).longValue();
        if (expireTime == -1) {
            stringRedisTemplate.opsForValue().increment(requestTimesKey);
            stringRedisTemplate.opsForValue().set(requestTimesKey, String.valueOf(1), rateLimitConfig.getMinutes(), TimeUnit.MINUTES);
        } else if (expireTime > 3) {
            // 过期时间太短时不再计数，避免频繁写入
            stringRedisTemplate.opsForValue().increment(requestTimesKey);
        }
    }

}
