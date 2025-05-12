package com.GDTW.shorturl.model;

import com.GDTW.general.exception.ShortUrlBannedException;
import com.GDTW.general.exception.ShortUrlNotFoundException;
import com.GDTW.general.util.codec.IdEncoderDecoderUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class ShortUrlService {

    private static final Logger logger = LoggerFactory.getLogger(ShortUrlService.class);
    private static final Duration TTL_DURATION = Duration.ofMinutes(10);

    private final ShortUrlJpa shortUrlJpa;
    private final RedisTemplate<String, String> redisStringStringTemplate;

    public ShortUrlService(ShortUrlJpa shortUrlJpa, @Qualifier("redisStringStringTemplate") RedisTemplate<String, String> redisTemplate) {
        this.shortUrlJpa = shortUrlJpa;
        this.redisStringStringTemplate = redisTemplate;
    }
    // ==================================================================
    // Service methods

    @Transactional
    public String createNewShortUrl(String originalUrl, String originalIp, String safeUrlResult) {
        Integer suId = recordOriginalUrl(originalUrl, originalIp, safeUrlResult);
        // Cache the mapping in Redis
        String encodedUrl = encodeShortUrl(suId);
        cacheShortUrl(encodedUrl, originalUrl);
        cacheShortUrlSafety(encodedUrl, safeUrlResult);
        return encodedUrl;
    }

    @Transactional(readOnly = true)
    public Map.Entry<String, String> getOriginalUrl(String code) {
        Integer suId = toDecodeSuId(code);

        String redisKeyForID = "su:suId:" + code;
        String redisKeyForSafe = "su:suSafe:" + code;
        String cachedOriginalUrl = redisStringStringTemplate.opsForValue().get(redisKeyForID);
        String cachedOriginalUrlSafe = redisStringStringTemplate.opsForValue().get(redisKeyForSafe);

        if (cachedOriginalUrl != null && cachedOriginalUrlSafe != null) {
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

    @Transactional(readOnly = true)
    public boolean isShortUrlIdExist(Integer suId) {
        return shortUrlJpa.existsBySuId(suId);
    }

    @Transactional(readOnly = true)
    public boolean isShortUrlValid(Integer suId) {
        return shortUrlJpa.checkShortUrlStatus(suId);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getSuIdAndSuSafe(Integer suId) {
        return shortUrlJpa.findSuIdAndSuSafeBySuId(suId);
    }

    @Transactional(readOnly = true)
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

    @Transactional
    public Integer recordOriginalUrl(String originalUrl, String originalIp, String safeUrlResult) {
        ShortUrlVO shortUrl = new ShortUrlVO();
        shortUrl.setSuOriginalUrl(originalUrl);
        shortUrl.setSuCreatedIp(originalIp);
        shortUrl.setSuCreatedDate(LocalDateTime.now());
        shortUrl.setSuStatus((byte) 0);
        shortUrl.setSuTotalUsed(0);
        shortUrl.setSuSafe(safeUrlResult);
        ShortUrlVO savedShortUrl = shortUrlJpa.save(shortUrl);
        return savedShortUrl.getSuId();
    }

    @Transactional
    public String encodeShortUrl(Integer suId) {
        Optional<ShortUrlVO> optionalShortUrl = shortUrlJpa.findById(suId);
        if (optionalShortUrl.isPresent()) {
            ShortUrlVO shortUrl = optionalShortUrl.get();
            String encodedUrl = toEncodeSuId(suId);
            shortUrl.setSuShortenedUrl(encodedUrl);
            shortUrlJpa.save(shortUrl);
            return encodedUrl;
        } else {
            throw new IllegalArgumentException("Invalid suId: " + suId);
        }
    }

    public void countShortUrlUsage(Integer suId) {
        String redisKey = "su:usage:" + suId; // prefix 'su:usage:'
        redisStringStringTemplate.opsForValue().increment(redisKey, 1);
    }

    @Scheduled(cron = "${task.schedule.cron.shortUtlUsageStatisticService}")
    @Transactional
    public void syncSuUsageToMySQL() {
        Set<String> keys = redisStringStringTemplate.keys("su:usage:*");
        if (keys != null) {
            for (String key : keys) {
                Integer suId = Integer.parseInt(key.split(":")[2]);
                Integer usageCount = Integer.parseInt(redisStringStringTemplate.opsForValue().get(key));

                Optional<ShortUrlVO> optionalShortUrl = shortUrlJpa.findById(suId);
                if (optionalShortUrl.isPresent()) {
                    ShortUrlVO shortUrl = optionalShortUrl.get();
                    shortUrl.setSuTotalUsed(shortUrl.getSuTotalUsed() + usageCount);
                    shortUrlJpa.save(shortUrl);
                    // delete recorded data in Redis
                    redisStringStringTemplate.delete(key);
                }
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
