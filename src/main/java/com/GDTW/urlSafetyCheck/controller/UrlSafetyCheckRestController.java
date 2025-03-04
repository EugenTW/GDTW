package com.GDTW.urlSafetyCheck.controller;

import com.GDTW.general.service.RateLimiterService;
import com.GDTW.urlSafetyCheck.model.UrlSafetyCheckService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/usc_api")
public class UrlSafetyCheckRestController {

    private static final Logger logger = LoggerFactory.getLogger(UrlSafetyCheckRestController.class);
    private final RedisTemplate<String, Integer> redisStringIntegerTemplate;
    private final RateLimiterService rateLimiterService;
    private final UrlSafetyCheckService urlSafetyCheckService;

    private static final String REDIS_CALL_COUNT_KEY = "URL_SAFETY_API_CALL_COUNT";
    private static final int DAILY_LIMIT = 4000;

    public UrlSafetyCheckRestController(RedisTemplate<String, Integer> redisStringIntegerTemplate, RateLimiterService rateLimiterService, UrlSafetyCheckService urlSafetyCheckService) {
        this.redisStringIntegerTemplate = redisStringIntegerTemplate;
        this.rateLimiterService = rateLimiterService;
        this.urlSafetyCheckService = urlSafetyCheckService;
    }

    @PostMapping("/check_url_safety")
    public Map<String, Object> checkUrlSafety(@RequestBody Map<String, Object> reqMap, HttpServletRequest request) {
        String clientIp = request.getRemoteAddr();
        rateLimiterService.checkGetUrlSafeCheckLimit(clientIp);

        Integer currentCount = redisStringIntegerTemplate.opsForValue().get(REDIS_CALL_COUNT_KEY);
        if (currentCount == null) {
            currentCount = 0;
        }

        Map<String, Object> result = new HashMap<>();

        if (currentCount >= DAILY_LIMIT) {
            logger.info("Daily API call limit reached: {}", currentCount);
            result.put("safeValue", "0");
            return result;
        }

        String inputUrl = (String) reqMap.get("original_url");
        String safeValue = urlSafetyCheckService.checkUrlSafety(inputUrl);

        redisStringIntegerTemplate.opsForValue().increment(REDIS_CALL_COUNT_KEY);
        result.put("safeValue", safeValue);

        return result;
    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void resetDailyCounter() {
        redisStringIntegerTemplate.opsForValue().set(REDIS_CALL_COUNT_KEY, 0);
        logger.info("The usage count for the UrlSafetyCheck API has been reset.");
    }

}
