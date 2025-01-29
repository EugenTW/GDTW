package com.GDTW.general.service;

import io.github.resilience4j.ratelimiter.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class RateLimiterService {

    private final ConcurrentMap<String, RateLimiter> rateLimiters = new ConcurrentHashMap<>();


    private RateLimiter createRateLimiter(int requestsPerSecond) {
        return RateLimiter.of("default",
                RateLimiterConfig.custom()
                        .limitRefreshPeriod(Duration.ofSeconds(1))
                        .limitForPeriod(requestsPerSecond)
                        .timeoutDuration(Duration.ofMillis(100))
                        .build()
        );
    }

    public void checkCreateShortUrlLimit(String clientIp) {
        rateLimiters.computeIfAbsent(clientIp, k -> createRateLimiter(5));

        if (!rateLimiters.get(clientIp).acquirePermission()) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "請求過於頻繁! Too many requests!");
        }
    }

    public void checkGetOriginalUrlLimit(String clientIp) {
        rateLimiters.computeIfAbsent(clientIp, k -> createRateLimiter(100));

        if (!rateLimiters.get(clientIp).acquirePermission()) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "請求過於頻繁! Too many requests!");
        }
    }

    public void checkCreateShareImageLimit(String clientIp) {
        rateLimiters.computeIfAbsent(clientIp, k -> createRateLimiter(2));

        if (!rateLimiters.get(clientIp).acquirePermission()) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "請求過於頻繁! Too many requests!");
        }
    }

    public void checkGetShareImageLimit(String clientIp) {
        rateLimiters.computeIfAbsent(clientIp, k -> createRateLimiter(25));

        if (!rateLimiters.get(clientIp).acquirePermission()) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "請求過於頻繁! Too many requests!");
        }
    }

}

