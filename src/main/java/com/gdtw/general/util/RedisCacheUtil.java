package com.gdtw.general.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@Component
public class RedisCacheUtil {

    private static final Logger logger = LoggerFactory.getLogger(RedisCacheUtil.class);
    private final RedisTemplate<String, Object> universalRedisTemplate;
    private final ObjectMapper objectMapper;

    public RedisCacheUtil(@Qualifier("universalRedisTemplate") RedisTemplate<String, Object> universalRedisTemplate, ObjectMapper objectMapper) {
        this.universalRedisTemplate = universalRedisTemplate;
        this.objectMapper = objectMapper;
    }

    public <T> Optional<T> getObject(String key, Class<T> clazz) {
        try {
            Object cached = universalRedisTemplate.opsForValue().get(key);
            if (cached == null) return Optional.empty();

            if (clazz.isInstance(cached)) {
                return Optional.of(clazz.cast(cached));
            } else if (cached instanceof Map<?, ?> map) {
                // Handle possible deserialization into LinkedHashMap when reloading
                T converted = objectMapper.convertValue(map, clazz);
                return Optional.of(converted);
            } else {
                logger.warn("Unexpected Redis object type for key: {}, expected: {}", key, clazz.getSimpleName());
                return Optional.empty();
            }
        } catch (Exception e) {
            logger.error("Failed to deserialize Redis value for key: {}", key, e);
            return Optional.empty();
        }
    }

    public void setObject(String key, Object value, Duration ttl) {
        try {
            universalRedisTemplate.opsForValue().set(key, value, ttl);
        } catch (Exception e) {
            logger.error("Failed to cache object to Redis for key: {}", key, e);
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getMap(String key) {
        try {
            Object cached = universalRedisTemplate.opsForValue().get(key);
            if (cached instanceof Map<?, ?> map) {
                return (Map<String, Object>) map;
            } else if (cached != null) {
                logger.warn("Unexpected type in Redis for key {}: {}", key, cached.getClass().getName());
            }
        } catch (Exception e) {
            logger.warn("Failed to read map from Redis for key: {}", key, e);
        }
        return Collections.emptyMap();
    }

}

