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
import java.time.LocalDateTime;
import java.util.*;

@Service
public class ShortUrlService {

    private static final Logger logger = LoggerFactory.getLogger(ShortUrlService.class);
    private static final Duration TTL_DURATION = Duration.ofMinutes(10);
    private static final String SHORT_URL_INFO_CACHE_PREFIX = "su:info:";
    private static final String USAGE_KEY_PREFIX = "su:usage:";

    private final ShortUrlJpa shortUrlJpa;
    private final RedisTemplate<String, String> redisStringStringTemplate;
    private final RedisTemplate<String, Object> universalRedisTemplate;


    public ShortUrlService(ShortUrlJpa shortUrlJpa, @Qualifier("redisStringStringTemplate") RedisTemplate<String, String> redisTemplate, @Qualifier("universalRedisTemplate") RedisTemplate<String, Object> universalRedisTemplate) {
        this.shortUrlJpa = shortUrlJpa;
        this.redisStringStringTemplate = redisTemplate;
        this.universalRedisTemplate = universalRedisTemplate;
    }
    // ==================================================================
    // Service methods

    @Transactional
    public String createNewShortUrl(String originalUrl, String originalIp, String safeUrlResult) {

        ShortUrlVO shortUrl = new ShortUrlVO();
        shortUrl.setSuOriginalUrl(originalUrl);
        shortUrl.setSuCreatedIp(originalIp);
        shortUrl.setSuCreatedDate(LocalDateTime.now());
        shortUrl.setSuStatus((byte) 0);
        shortUrl.setSuTotalUsed(0);
        shortUrl.setSuSafe(safeUrlResult);

        ShortUrlVO saved = shortUrlJpa.save(shortUrl);
        Integer suId = saved.getSuId();

        String encoded = IdEncoderDecoderUtil.encodeId(suId);
        saved.setSuShortenedUrl(encoded);

        ShortUrlInfoDTO dto = new ShortUrlInfoDTO(
                suId,
                originalUrl,
                encoded,
                0,
                (byte) 0,
                safeUrlResult
        );
        cacheShortUrlInfo(dto);

        return encoded;
    }

    public Map.Entry<String, String> getOriginalUrl(String code) {

        Integer suId;
        try {
            suId = IdEncoderDecoderUtil.decodeId(code);
        } catch (Exception e) {
            throw new ShortUrlNotFoundException("短碼無效! Invalid short code!");
        }

        ShortUrlInfoDTO dto = getOrCacheShortUrlInfo(suId);

        if (dto.getSuStatus() != 0) {
            throw new ShortUrlBannedException("此短網址已失效! The short URL is banned.");
        }

        countShortUrlUsage(suId);
        return new AbstractMap.SimpleEntry<>(dto.getSuOriginalUrl(), dto.getSuSafe());
    }


    public boolean checkCodeValid(String code) {
        Integer suId;
        try {
            suId = IdEncoderDecoderUtil.decodeId(code);
        } catch (Exception e) {
            return false;
        }

        ShortUrlInfoDTO dto = getOrCacheShortUrlInfo(suId);

        return dto.getSuStatus() == 0;
    }

    // ==================================================================
    // Redis caching methods

    private void cacheShortUrlInfo(ShortUrlInfoDTO dto) {
        if (dto == null || dto.getSuId() == null) {
            return;
        }
        String redisKey = SHORT_URL_INFO_CACHE_PREFIX + dto.getSuId();
        universalRedisTemplate.opsForValue().set(redisKey, dto, TTL_DURATION);
    }

    private ShortUrlInfoDTO getOrCacheShortUrlInfo(Integer suId) {

        String redisKey = SHORT_URL_INFO_CACHE_PREFIX + suId;
        Object cached = universalRedisTemplate.opsForValue().get(redisKey);
        if (cached instanceof ShortUrlInfoDTO dto) {
            return dto;
        }

        Optional<ShortUrlVO> optional = shortUrlJpa.findById(suId);
        if (optional.isEmpty()) {
            throw new ShortUrlNotFoundException("此短網址尚未建立! Original URL not found!");
        }

        ShortUrlVO vo = optional.get();
        ShortUrlInfoDTO dto = new ShortUrlInfoDTO(
                vo.getSuId(),
                vo.getSuOriginalUrl(),
                vo.getSuShortenedUrl(),
                vo.getSuTotalUsed(),
                vo.getSuStatus(),
                vo.getSuSafe()
        );

        universalRedisTemplate.opsForValue().set(redisKey, dto, TTL_DURATION);
        return dto;
    }

    // ==================================================================
    // Recording methods

    public void countShortUrlUsage(Integer suId) {
        String redisKey = USAGE_KEY_PREFIX + suId;
        redisStringStringTemplate.opsForValue().increment(redisKey, 1);
    }

    @Scheduled(cron = "${task.schedule.cron.shortUtlUsageStatisticService}")
    @Transactional
    public void syncSuUsageToMySQL() {
        Set<String> keys = redisStringStringTemplate.keys(USAGE_KEY_PREFIX + "*");
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

}
