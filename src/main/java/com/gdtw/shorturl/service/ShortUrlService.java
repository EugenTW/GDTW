package com.gdtw.shorturl.service;

import com.gdtw.general.exception.ShortUrlBannedException;
import com.gdtw.general.exception.ShortUrlNotFoundException;
import com.gdtw.general.helper.ServiceUsageCounterHelper;
import com.gdtw.general.helper.RedisObjectCacheHelper;
import com.gdtw.general.util.CodecShortUrlIdUtil;
import com.gdtw.shorturl.model.ShortUrlInfoDTO;
import com.gdtw.shorturl.repository.ShortUrlJpa;
import com.gdtw.shorturl.model.ShortUrlVO;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class ShortUrlService {

    private static final Duration TTL_DURATION = Duration.ofMinutes(5);
    private static final String SHORT_URL_INFO_CACHE_PREFIX = "su:info:";
    private static final String USAGE_KEY_PREFIX = "su:usage:";

    private final ShortUrlJpa shortUrlJpa;
    private final ServiceUsageCounterHelper serviceUsageCounterHelper;
    private final RedisObjectCacheHelper redisObjectCacheUtil;
    private final RedisTemplate<String,Integer> redisStringIntegerTemplate;
    private final RedisTemplate<String, Object> universalRedisTemplate;

    public ShortUrlService(ShortUrlJpa shortUrlJpa, ServiceUsageCounterHelper serviceUsageCounterHelper, RedisObjectCacheHelper redisCacheHelper, @Qualifier("redisStringIntegerTemplate") RedisTemplate<String, Integer> redisStringIntegerTemplate, @Qualifier("universalRedisTemplate") RedisTemplate<String, Object> universalRedisTemplate) {
        this.shortUrlJpa = shortUrlJpa;
        this.serviceUsageCounterHelper = serviceUsageCounterHelper;
        this.redisObjectCacheUtil = redisCacheHelper;
        this.redisStringIntegerTemplate = redisStringIntegerTemplate;
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

        String encoded = CodecShortUrlIdUtil.encodeId(suId);
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
            suId = CodecShortUrlIdUtil.decodeId(code);
        } catch (Exception e) {
            throw new ShortUrlNotFoundException("短碼無效! Invalid short code!");
        }

        ShortUrlInfoDTO dto = getOrCacheShortUrlInfo(suId);

        if (dto.getSuStatus() != 0) {
            throw new ShortUrlBannedException("此短網址已失效! The short URL is banned.");
        }

        serviceUsageCounterHelper.countServiceUsage(USAGE_KEY_PREFIX, suId);
        return new AbstractMap.SimpleEntry<>(dto.getSuOriginalUrl(), dto.getSuSafe());
    }

    @Transactional
    public void reportShortUrl(Integer shortUrlId, Map<String, String> result) {
        int updatedRows = shortUrlJpa.incrementReportIfNotBlocked(shortUrlId);

        if (updatedRows == 1) {
            result.put("reportStatus", "true");
            result.put("response", "舉報成功！\nReport successful!");
        } else {
            boolean exists = shortUrlJpa.existsById(shortUrlId);
            result.put("reportStatus", "false");
            if (!exists) {
                result.put("response", "查無此短網址。\nShort URL not found.");
            } else {
                result.put("response", "該網址已封鎖，無法再次舉報。\nThis URL has been blocked, cannot report again.");
            }
        }
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

        Optional<ShortUrlInfoDTO> optional = redisObjectCacheUtil.getObject(redisKey, ShortUrlInfoDTO.class);
        if (optional.isPresent()) {
            return optional.get();
        }

        Optional<ShortUrlVO> optionalVO = shortUrlJpa.findById(suId);
        if (optionalVO.isEmpty()) {
            throw new ShortUrlNotFoundException("此短網址尚未建立! Original URL not found!");
        }

        ShortUrlVO vo = optionalVO.get();
        ShortUrlInfoDTO dto = new ShortUrlInfoDTO(
                vo.getSuId(),
                vo.getSuOriginalUrl(),
                vo.getSuShortenedUrl(),
                vo.getSuTotalUsed(),
                vo.getSuStatus(),
                vo.getSuSafe()
        );

        redisObjectCacheUtil.setObject(redisKey, dto, TTL_DURATION);
        return dto;
    }

    // ==================================================================
    // Recording methods

    @Transactional
    public void syncSuUsageToMySQL() {
        Set<String> keys = redisStringIntegerTemplate.keys(USAGE_KEY_PREFIX + "*");
        for (String key : keys) {
            Integer suId = Integer.parseInt(key.split(":")[2]);
            Integer usageCount = redisStringIntegerTemplate.opsForValue().get(key);
            int usage = (usageCount != null) ? usageCount : 0;

            Optional<ShortUrlVO> optionalShortUrl = shortUrlJpa.findById(suId);

            if (optionalShortUrl.isPresent()) {
                ShortUrlVO shortUrl = optionalShortUrl.get();
                shortUrl.setSuTotalUsed(shortUrl.getSuTotalUsed() + usage);
                shortUrlJpa.save(shortUrl);
                // delete recorded data in Redis
                redisStringIntegerTemplate.delete(key);
            }
        }
    }

}
