package com.GDTW.general.service;

import io.github.resilience4j.ratelimiter.*;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.*;

@Service
public class RateLimiterService {

    private final ConcurrentMap<String, RateLimiter> rateLimiters = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Long> lastAccessTime = new ConcurrentHashMap<>();
    private static final long EXPIRATION_TIME_MS = Duration.ofMinutes(5).toMillis();

    private RateLimiter createRateLimiter(int requestsPerSecond) {
        return RateLimiter.of("default",
                RateLimiterConfig.custom()
                        .limitRefreshPeriod(Duration.ofSeconds(1))
                        .limitForPeriod(requestsPerSecond)
                        .timeoutDuration(Duration.ofMillis(100))
                        .build()
        );
    }

    private void checkRateLimit(String clientIp, int requestLimit) {

        if (clientIp == null || "0:0:0:0:0:0:0:1".equals(clientIp) || "127.0.0.1".equals(clientIp)) {
            clientIp = "localhost";
        }

        rateLimiters.computeIfAbsent(clientIp, k -> createRateLimiter(requestLimit));
        lastAccessTime.put(clientIp, System.currentTimeMillis());

        if (!rateLimiters.get(clientIp).acquirePermission()) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "請求過於頻繁! Too many requests!");
        }
    }

    public void checkCreateShortUrlLimit(String clientIp) {
        checkRateLimit(clientIp, 5);
    }

    public void checkGetOriginalUrlLimit(String clientIp) {
        checkRateLimit(clientIp, 20);
    }

    public void checkCreateShareImageLimit(String clientIp) {
        checkRateLimit(clientIp, 5);
    }

    public void checkGetShareImageLimit(String clientIp) {
        checkRateLimit(clientIp, 20);
    }

    public void checkGetDailyStatisticLimit(String clientIp) {
        checkRateLimit(clientIp, 20);
    }

    public void checkGetUrlSafeCheckLimit(String clientIp) {
        checkRateLimit(clientIp, 5);
    }

    @Scheduled(fixedRate = 300000)
    public void cleanUpRateLimiters() {
        long now = System.currentTimeMillis();
        for (Map.Entry<String, Long> entry : lastAccessTime.entrySet()) {
            String ip = entry.getKey();
            long lastUsedTime = entry.getValue();

            if (now - lastUsedTime > EXPIRATION_TIME_MS) {
                rateLimiters.remove(ip);
                lastAccessTime.remove(ip);
            }
        }
    }

}
