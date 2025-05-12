package com.gdtw.shorturl.model;

import com.gdtw.general.exception.ShortUrlBannedException;
import com.gdtw.general.exception.ShortUrlNotFoundException;
import com.gdtw.general.util.codec.IdEncoderDecoderUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.*;

@Service
public class ShortUrlService {

    private static final Logger logger = LoggerFactory.getLogger(ShortUrlService.class);
    private static final Duration TTL_DURATION = Duration.ofMinutes(10);

    private final ShortUrlInternalService shortUrlInternalService;
    private final ShortUrlJpa shortUrlJpa;
    private final RedisTemplate<String, String> redisStringStringTemplate;

    public ShortUrlService(ShortUrlInternalService shortUrlInternalService, ShortUrlJpa shortUrlJpa, @Qualifier("redisStringStringTemplate") RedisTemplate<String, String> redisTemplate) {
        this.shortUrlInternalService = shortUrlInternalService;
        this.shortUrlJpa = shortUrlJpa;
        this.redisStringStringTemplate = redisTemplate;
    }
    // ==================================================================
    // Service methods

    @Transactional
    public String createNewShortUrl(String originalUrl, String originalIp, String safeUrlResult) {
        Integer suId = shortUrlInternalService.recordOriginalUrl(originalUrl, originalIp, safeUrlResult);
        String encodedUrl = shortUrlInternalService.encodeShortUrl(suId);
        cacheShortUrl(encodedUrl, originalUrl);
        cacheShortUrlSafety(encodedUrl, safeUrlResult);
        return encodedUrl;
    }

    public Map.Entry<String, String> getOriginalUrl(String code) {
        Integer suId = toDecodeSuId(code);

        String redisKeyForID = "su:suId:" + code;
        String redisKeyForSafe = "su:suSafe:" + code;
        String cachedOriginalUrl = redisStringStringTemplate.opsForValue().get(redisKeyForID);
        String cachedOriginalUrlSafe = redisStringStringTemplate.opsForValue().get(redisKeyForSafe);

        if (cachedOriginalUrl != null && !cachedOriginalUrl.isBlank() &&
                cachedOriginalUrlSafe != null && !cachedOriginalUrlSafe.isBlank()) {
            countShortUrlUsage(suId);
            return new AbstractMap.SimpleEntry<>(cachedOriginalUrl, cachedOriginalUrlSafe);
        }

        if (!isShortUrlIdExist(suId)) {
            throw new ShortUrlNotFoundException("此短網址尚未建立! Original URL not found!");
        }

        if (!isShortUrlValid(suId)) {
            throw new ShortUrlBannedException("此短網址已失效! The short URL is banned.");
        }

        Map<String, Object> result = getSuIdAndSuSafe(suId);
        String originalUrl = (String) result.get("suOriginalUrl");
        String originalUrlSafe = (String) result.get("suSafe");

        cacheShortUrl(code, originalUrl);
        cacheShortUrlSafety(code, originalUrlSafe);
        countShortUrlUsage(suId);

        return new AbstractMap.SimpleEntry<>(originalUrl, originalUrlSafe);
    }

    // ==================================================================
    // Redis caching methods
    private void cacheShortUrl(String encodedSuId, String originalUrl) {
        String redisKey = "su:suId:" + encodedSuId; // prefix 'su:suId:'
        redisStringStringTemplate.opsForValue().set(redisKey, originalUrl, TTL_DURATION);
    }

    private void cacheShortUrlSafety(String encodedSuId, String safeUrlResult) {
        String redisKey = "su:suSafe:" + encodedSuId;// prefix 'su:suSafe:'
        redisStringStringTemplate.opsForValue().set(redisKey, safeUrlResult, TTL_DURATION);
    }

    // ==================================================================
    // Read-only methods

    public boolean isShortUrlIdExist(Integer suId) {
        return shortUrlJpa.existsBySuId(suId);
    }

    public boolean isShortUrlValid(Integer suId) {
        return shortUrlJpa.checkShortUrlStatus(suId);
    }

    public Map<String, Object> getSuIdAndSuSafe(Integer suId) {
        return shortUrlJpa.findSuIdAndSuSafeBySuId(suId);
    }

    public boolean checkCodeValid(String code) {
        Integer suId = toDecodeSuId(code);
        // Check Redis cache first
        String redisKey = "su:suId:" + code; // add prefix 'su:'
        String cachedOriginalUrl = redisStringStringTemplate.opsForValue().get(redisKey);
        if (cachedOriginalUrl != null) {
            return true;
        }
        return isShortUrlIdExist(suId) && isShortUrlValid(suId);
    }

    // ==================================================================
    // Writing methods

    public void countShortUrlUsage(Integer suId) {
        String redisKey = "su:usage:" + suId; // prefix 'su:usage:'
        redisStringStringTemplate.opsForValue().increment(redisKey, 1);
    }

    @Scheduled(cron = "${task.schedule.cron.shortUtlUsageStatisticService}")
    @Transactional
    public void syncSuUsageToMySQL() {
        Set<String> keys = redisStringStringTemplate.keys("su:usage:*");
        for (String key : keys) {
            Integer suId = Integer.parseInt(key.split(":")[2]);
            String rawValue = redisStringStringTemplate.opsForValue().get(key);
            if (rawValue == null) {
                logger.warn("Missing Redis value for key '{}', skipping...", key);
                continue;
            }
            Integer usageCount = Integer.parseInt(rawValue);

            Optional<ShortUrlVO> optionalShortUrl = shortUrlJpa.findById(suId);
            if (optionalShortUrl.isPresent()) {
                ShortUrlVO shortUrl = optionalShortUrl.get();
                shortUrl.setSuTotalUsed(shortUrl.getSuTotalUsed() + usageCount);
                shortUrlJpa.save(shortUrl);
                // delete recorded data in Redis
                redisStringStringTemplate.delete(key);
            }
        }
        logger.info("Sync 'Short URL' usage to MySQL!");
    }

    // ==================================================================
    // Supporting methods

    public static String toEncodeSuId(Integer suId) {
        return IdEncoderDecoderUtil.encodeId(suId);
    }

    public static Integer toDecodeSuId(String encodeSuId) {
        return IdEncoderDecoderUtil.decodeId(encodeSuId);
    }

}
