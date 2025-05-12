package com.gdtw.general.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisService {

    private static final Logger logger = LoggerFactory.getLogger(RedisService.class);

    private final RedisTemplate<String, ?> redisTemplate;

    public RedisService(@Qualifier("universalRedisTemplate") RedisTemplate<String, ?> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void clearRedis(boolean confirm) {
        if (!confirm) {
            logger.warn("Redis flush skipped. Confirmation flag not set.");
            return;
        }
        RedisConnectionFactory connectionFactory = redisTemplate.getConnectionFactory();
        if (connectionFactory == null) {
            logger.warn("Redis connection factory is null. Skipping Redis flush.");
            return;
        }
        try (RedisConnection connection = connectionFactory.getConnection()) {
            connection.serverCommands().flushDb();
            logger.info("Redis database has been cleared.");
        } catch (Exception e) {
            logger.error("Failed to clear Redis database: " + e.getMessage(), e);
        }
    }
}
