package com.moyz.adi.common.util;

import dev.langchain4j.model.output.TokenUsage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.text.MessageFormat;
import java.time.Duration;
import java.util.List;

import static com.moyz.adi.common.cosntant.RedisKeyConstant.TOKEN_USAGE_KEY;
/**
 * 大模型令牌工具类。
 */

@Slf4j
public class LLMTokenUtil {

    /**
     * 缓存 token 使用情况。
     *
     * @param stringRedisTemplate Redis 模板
     * @param uuid                唯一标识
     * @param tokenUsage          token 使用量
     * @return 无
     */
    public static void cacheTokenUsage(StringRedisTemplate stringRedisTemplate, String uuid, TokenUsage tokenUsage) {
        String redisKey = MessageFormat.format(TOKEN_USAGE_KEY, uuid);
        // 设置短 TTL，避免 token 统计长时间堆积
        stringRedisTemplate.expire(redisKey, Duration.ofMinutes(10));
        // 按“输入、输出”顺序追加，便于后续成对累计
        stringRedisTemplate.opsForList().rightPushAll(redisKey, String.valueOf(tokenUsage.inputTokenCount()), String.valueOf(tokenUsage.outputTokenCount()));
    }

    /**
     * 计算缓存在 Redis 中的 token 使用情况。
     *
     * @param stringRedisTemplate Redis 模板
     * @param uuid                唯一标识
     * @return Pair<Integer, Integer> Pair<输入 token 数量, 输出 token 数量>
     */
    public static Pair<Integer, Integer> calAllTokenCostByUuid(StringRedisTemplate stringRedisTemplate, String uuid) {
        List<String> tokenCountList = stringRedisTemplate.opsForList().range(MessageFormat.format(TOKEN_USAGE_KEY, uuid), 0, -1);
        int inputTokenCount = 0;
        int outputTokenCount = 0;
        if (!CollectionUtils.isEmpty(tokenCountList) && tokenCountList.size() > 1) {
            // 按“输入、输出”成对累加，保持与缓存顺序一致
            int tokenCountListSize = tokenCountList.size();
            int i = 0;
            while (i < tokenCountListSize) {
                inputTokenCount += Integer.parseInt(tokenCountList.get(i));
                i++;
                if (i < tokenCountListSize) {
                    outputTokenCount += Integer.parseInt(tokenCountList.get(i));
                }
                i++;
            }
        }
        return Pair.of(inputTokenCount, outputTokenCount);
    }
}
