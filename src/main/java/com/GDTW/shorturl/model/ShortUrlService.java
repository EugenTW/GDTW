package com.GDTW.shorturl.model;

import com.GDTW.service.IdEncoderDecoderService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Date;
import java.util.Optional;
import java.util.Set;

@Service
public class ShortUrlService {

    private static final Logger logger = LoggerFactory.getLogger(ShortUrlService.class);

    private final ShortUrlJpa shortUrlJpa;
    private final RedisTemplate<String, String> redisTemplate;

    private static final Duration TTL_DURATION = Duration.ofHours(24);

    public ShortUrlService(ShortUrlJpa shortUrlJpa, RedisTemplate<String, String> redisTemplate) {
        this.shortUrlJpa = shortUrlJpa;
        this.redisTemplate = redisTemplate;
    }
    // ==================================================================
    // Service methods

    @Transactional
    public String createNewShortUrl(String originalUrl, String originalIp) {
        Integer suId = recordOriginalUrl(originalUrl, originalIp);
        // Cache the mapping in Redis
        String encodedUrl = encodeShortUrl(suId);
        cacheShortUrl(encodedUrl, originalUrl);
        return encodedUrl;
    }

    @Transactional
    public String getOriginalUrl(String code) {
        Integer suId = toDecodeSuId(code);
        // Check Redis cache first
        String redisKey = "su:suId:" + code; // add prefix 'su:'
        String cachedOriginalUrl = redisTemplate.opsForValue().get(redisKey);
        if (cachedOriginalUrl != null) {
            countShortUrlUsage(suId);
            return cachedOriginalUrl;
        }
        if (!isShortUrlIdExist(suId)) {
            return "na";
        }
        if (!isShortUrlValid(suId)) {
            return "ban";
        }
        String originalUrl = getOriginalUrl(suId);
        cacheShortUrl(code, originalUrl);
        countShortUrlUsage(suId);
        return getOriginalUrl(suId);
    }

    // ==================================================================
    // Redis caching methods
    private void cacheShortUrl(String encodedSuId, String originalUrl) {
        String redisKey = "su:suId:" + encodedSuId; // prefix 'su:suId:'
        redisTemplate.opsForValue().set(redisKey, originalUrl, TTL_DURATION);
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
    public boolean isShortUrlHavingUId(Integer suId) {
        return shortUrlJpa.checkShortUrlCreator(suId);
    }

    @Transactional(readOnly = true)
    public ShortUrlVO getAllDataOfShortUrl(Integer suId) {
        return shortUrlJpa.findBySuId(suId);
    }

    @Transactional(readOnly = true)
    public String getShortenUrl(Integer suId) {
        return shortUrlJpa.findSuShortenedUrlBySuId(suId);
    }

    @Transactional(readOnly = true)
    public String getOriginalUrl(Integer suId) {
        return shortUrlJpa.findOriginalUrlBySuId(suId);
    }

    @Transactional(readOnly = true)
    public boolean checkCodeValid(String code) {
        Integer suId = toDecodeSuId(code);
        // Check Redis cache first
        String redisKey = "su:suId:" + code; // add prefix 'su:'
        String cachedOriginalUrl = redisTemplate.opsForValue().get(redisKey);
        if (cachedOriginalUrl != null) {
            return true;
        }
        return isShortUrlIdExist(suId) && isShortUrlValid(suId);
    }

    // ==================================================================
    // Writing methods

    @Transactional
    public Integer recordOriginalUrl(String originalUrl, String originalIp) {
        ShortUrlVO shortUrl = new ShortUrlVO();
        shortUrl.setSuOriginalUrl(originalUrl);
        shortUrl.setSuCreatedIp(originalIp);
        shortUrl.setSuCreatedDate(new Date());
        shortUrl.setSuStatus(0);
        shortUrl.setSuTotalUsed(0);
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

    @Transactional
    public void countShortUrlUsage(Integer suId) {
        String redisKey = "su:usage:" + suId; // prefix 'su:usage:'
        redisTemplate.opsForValue().increment(redisKey, 1);
    }

    // Scheduled task to run every two hours at 55 minutes past the hour
    @Scheduled(cron = "0 55 0/2 * * ?")
    @Transactional
    public void syncUsageToMySQL() {
        Set<String> keys = redisTemplate.keys("su:usage:*");
        if (keys != null) {
            for (String key : keys) {
                Integer suId = Integer.parseInt(key.split(":")[2]);
                Integer usageCount = Integer.parseInt(redisTemplate.opsForValue().get(key));

                Optional<ShortUrlVO> optionalShortUrl = shortUrlJpa.findById(suId);
                if (optionalShortUrl.isPresent()) {
                    ShortUrlVO shortUrl = optionalShortUrl.get();
                    shortUrl.setSuTotalUsed(shortUrl.getSuTotalUsed() + usageCount);
                    shortUrlJpa.save(shortUrl);
                    // delete recorded data in Redis
                    redisTemplate.delete(key);
                }
            }
        }
    }

    // ==================================================================
    // Supporting methods

    public static String toEncodeSuId(Integer id) {
        return IdEncoderDecoderService.encodeId(id);
    }

    public static Integer toDecodeSuId(String encodeSuId) {
        return IdEncoderDecoderService.decodeId(encodeSuId);
    }

}
