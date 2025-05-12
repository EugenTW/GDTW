package com.gdtw.general.service.ratelimiter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;

@Service
public class RateLimiterService {

    private final RedisTemplate<String, String> redisTemplate;
    private static final Logger logger = LoggerFactory.getLogger(RateLimiterService.class);

    private volatile long lastRedisErrorLogTime = 0;

    public RateLimiterService(@Qualifier("redisStringStringTemplate") RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void checkLimit(String clientIp, RateLimitRule rule) {
        try {
            doRateLimitCheck(clientIp, rule);
        } catch (Exception e) {
            long now = System.currentTimeMillis();
            if (now - lastRedisErrorLogTime > Duration.ofMinutes(15).toMillis()) {
                lastRedisErrorLogTime = now;
                logger.error("Redis unavailable. Rate limit check skipped.", e);
            }
        }
    }

    private void doRateLimitCheck(String clientIp, RateLimitRule rule) {
        if (clientIp == null || clientIp.isBlank() || "0:0:0:0:0:0:0:1".equals(clientIp) || "127.0.0.1".equals(clientIp)) {
            clientIp = "localhost";
        }

        String globalKey = "ratelimit:global";
        Long globalCount = redisTemplate.opsForValue().increment(globalKey);
        if (globalCount != null) {
            handleGlobalRateLimit(globalCount);
        }

        String actionKey = rule.actionKey;
        int limit = rule.limit;
        int seconds = rule.durationSeconds;

        String banKey = String.format("banlist:%s", clientIp);
        Boolean isBanned = redisTemplate.hasKey(banKey);
        if (Boolean.TRUE.equals(isBanned)) {
            logger.warn("Blocked IP tried to access: '{}'.", clientIp);
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                    "您暫時被封鎖，請稍後再試! - You are temporarily banned. Please try again later.");
        }

        String redisKey = String.format("ratelimit:%s:%s", actionKey, clientIp);
        Long count = redisTemplate.opsForValue().increment(redisKey);
        if (count != null && count == 1) {
            redisTemplate.expire(redisKey, Duration.ofSeconds(seconds));
        }

        if (count != null && count > limit) {
            handlePerIpRateLimitViolation(actionKey, clientIp, banKey);
        }
    }

    private void handleGlobalRateLimit(long globalCount) {
        if (globalCount == 1) {
            redisTemplate.expire("ratelimit:global", Duration.ofSeconds(1));
        }

        if (globalCount == 2001) {
            logger.warn("High traffic warning: globalCount exceeds 2000 RPS. Current count: {}.", globalCount);
        }

        if (globalCount == 2501) {
            logger.warn("Global request limit exceeded threshold (2500 RPS). Current count: {}.", globalCount);
        }

        if (globalCount > 2500) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                    "全站請求過多，請稍後再試! - Global rate limit exceeded. Please try again later.");
        }
    }

    private void handlePerIpRateLimitViolation(String actionKey, String clientIp, String banKey) {
        String violationKey = String.format("ratelimit:violation:%s:%s", actionKey, clientIp);
        Long violations = redisTemplate.opsForValue().increment(violationKey);

        if (violations != null && redisTemplate.getExpire(violationKey) == -1) {
            redisTemplate.expire(violationKey, Duration.ofMinutes(15));
        }

        logger.warn("Rate limit exceeded by IP: {}, action: {}, violation count: {}.", clientIp, actionKey, violations);

        if (violations != null && violations >= 5) {
            redisTemplate.opsForValue().set(banKey, "1", Duration.ofMinutes(15));
            logger.warn("IP {} has been banned for 15 minutes due to repeated violations.", clientIp);
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                    "請求過於頻繁，您已被封鎖 15 分鐘! - Too many requests. You have been banned for 15 minutes.");
        }

        throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                "請求過於頻繁! - Too many requests!");
    }

    public void checkCreateShortUrlLimit(String clientIp) {
        checkLimit(clientIp, RateLimitRule.SHORTURL_CREATE);
    }

    public void checkGetOriginalUrlLimit(String clientIp) {
        checkLimit(clientIp, RateLimitRule.SHORTURL_GET);
    }

    public void checkCreateShareImageLimit(String clientIp) {
        checkLimit(clientIp, RateLimitRule.SHAREIMAGE_CREATE);
    }

    public void checkGetShareImageLimit(String clientIp) {
        checkLimit(clientIp, RateLimitRule.SHAREIMAGE_GET);
    }

    public void checkGetDailyStatisticLimit(String clientIp) {
        checkLimit(clientIp, RateLimitRule.STATISTIC_GET);
    }

    public void checkGetUrlSafeCheckLimit(String clientIp) {
        checkLimit(clientIp, RateLimitRule.URLSAFE_GET);
    }

}
