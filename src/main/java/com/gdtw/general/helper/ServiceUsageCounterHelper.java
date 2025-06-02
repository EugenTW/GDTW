package com.gdtw.general.helper;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class ServiceUsageCounterHelper {

    private final RedisTemplate<String, Integer> redisStringIntegerTemplate;

    public ServiceUsageCounterHelper(RedisTemplate<String, Integer> redisStringIntegerTemplate) {
        this.redisStringIntegerTemplate = redisStringIntegerTemplate;
    }

    public void countServiceUsage(String servicePrefix, Integer id) {
        String redisKey = servicePrefix + id;
        redisStringIntegerTemplate.opsForValue().increment(redisKey, 1);
    }

}
