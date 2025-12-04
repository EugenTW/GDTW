
package com.gdtw.general.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean(name = "redisStringStringTemplate")
    public RedisTemplate<String, String> redisStringStringTemplate(
            RedisConnectionFactory connectionFactory
    ) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setValueSerializer(stringSerializer);

        template.setHashKeySerializer(stringSerializer);
        template.setHashValueSerializer(stringSerializer);

        template.afterPropertiesSet();
        return template;
    }

    @Bean(name = "redisStringIntegerTemplate")
    public RedisTemplate<String, Integer> redisStringIntegerTemplate(
            RedisConnectionFactory connectionFactory
    ) {
        RedisTemplate<String, Integer> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        GenericToStringSerializer<Integer> intSerializer = new GenericToStringSerializer<>(Integer.class);

        template.setKeySerializer(stringSerializer);
        template.setValueSerializer(intSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setHashValueSerializer(intSerializer);

        template.afterPropertiesSet();
        return template;
    }

    @Bean(name = "universalRedisTemplate")
    public RedisTemplate<String, Object> universalRedisTemplate(
            RedisConnectionFactory connectionFactory
    ) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        RedisSerializer<Object> json = RedisSerializer.json(); // 由 Spring Data Redis 提供
        StringRedisSerializer stringSerializer = new StringRedisSerializer();

        template.setKeySerializer(stringSerializer);
        template.setValueSerializer(json);
        template.setHashKeySerializer(stringSerializer);
        template.setHashValueSerializer(json);

        template.afterPropertiesSet();
        return template;
    }
}
