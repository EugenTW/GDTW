package com.gdtw.dailystatistic.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.util.*;

@Service
public class DailyStatisticService {

    private static final String REDIS_KEY_TOTAL_STATS_PREFIX = "ds:totalStatistics:";
    private static final String REDIS_KEY_STATISTIC_PREFIX = "statistic:";

    private static final Logger logger = LoggerFactory.getLogger(DailyStatisticService.class);
    private final RedisTemplate<String, String> redisStringStringTemplate;
    private final RedisTemplate<String, Integer> redisStringIntegerTemplate;
    private final ObjectMapper objectMapper;
    private static final Duration TTL_DURATION = Duration.ofHours(25);
    private final DailyStatisticJpa dailyStatisticJpa;

    public DailyStatisticService(
            @Qualifier("redisStringStringTemplate") RedisTemplate<String, String> redisStringStringTemplate,
            @Qualifier("redisStringIntegerTemplate") RedisTemplate<String, Integer> redisStringIntegerTemplate,
            ObjectMapper objectMapper,
            DailyStatisticJpa dailyStatisticJpa) {
        this.redisStringStringTemplate = redisStringStringTemplate;
        this.redisStringIntegerTemplate = redisStringIntegerTemplate;
        this.objectMapper = objectMapper;
        this.dailyStatisticJpa = dailyStatisticJpa;
    }

    // =============================================================
    // calculate the total usage count of each service
    public TotalServiceStatisticsDTO getTotalServiceStatistics() {
        LocalDate today = LocalDate.now();
        String key = REDIS_KEY_TOTAL_STATS_PREFIX + today;
        TotalServiceStatisticsDTO savedResultInRedis = getTotalServiceStatisticsDtoFromRedis(key);
        if (savedResultInRedis != null) {
            return savedResultInRedis;
        }
        return calculateTotalServiceStatistics();
    }

    @Scheduled(cron = "${task.schedule.cron.dailyCalculateTotalServiceStatistics}")
    public TotalServiceStatisticsDTO calculateTotalServiceStatistics() {
        LocalDate today = LocalDate.now();
        Object[] result = dailyStatisticJpa.calculateSumsBeforeDate(today);

        TotalServiceStatisticsDTO totalStatistics = new TotalServiceStatisticsDTO();
        String key = REDIS_KEY_TOTAL_STATS_PREFIX + today;
        if (result == null || result.length == 0) {
            saveTotalServiceStatisticsDtoToRedis(key, totalStatistics);
            return totalStatistics;
        }

        Object[] row = (Object[]) result[0];
        int[] safeValues = new int[9];
        for (int i = 0; i < safeValues.length; i++) {
            if (row[i] instanceof Number number) {
                safeValues[i] = number.intValue();
            } else {
                safeValues[i] = 0;
            }
        }

        totalStatistics.setTotalShortUrlsCreated(safeValues[0]);
        totalStatistics.setTotalShortUrlsUsed(safeValues[1]);
        totalStatistics.setTotalImagesCreated(safeValues[2]);
        totalStatistics.setTotalImagesVisited(safeValues[3]);
        totalStatistics.setTotalImageAlbumsCreated(safeValues[4]);
        totalStatistics.setTotalImageAlbumsVisited(safeValues[5]);
        totalStatistics.setTotalWebServiceCount(safeValues[6]);
        totalStatistics.setTotalCssJsMinified(safeValues[7]);
        totalStatistics.setTotalWebpConverted(safeValues[8]);

        saveTotalServiceStatisticsDtoToRedis(key, totalStatistics);
        return totalStatistics;
    }

    private void saveTotalServiceStatisticsDtoToRedis(String key, TotalServiceStatisticsDTO dto) {
        try {
            String json = objectMapper.writeValueAsString(dto);
            redisStringStringTemplate.opsForValue().set(key, json, TTL_DURATION);
        } catch (Exception e) {
            logger.error("Failed to save TotalServiceStatisticsDTO to Redis. Key: {}.", key, e);

        }
    }

    private TotalServiceStatisticsDTO getTotalServiceStatisticsDtoFromRedis(String key) {
        try {
            String json = redisStringStringTemplate.opsForValue().get(key);
            if (json != null) {
                return objectMapper.readValue(json, TotalServiceStatisticsDTO.class);
            }
        } catch (Exception e) {
            logger.error("Failed to retrieve TotalServiceStatisticsDTO from Redis. Key: {}.", key, e);
        }
        return null;
    }

    // =============================================================
    // retrieve daily data for each service for up to the past year
    public ChartDataDTO getRecentStatisticsForCharts() {
        LocalDate today = LocalDate.now();
        ChartDataDTO cachedChartData = getChartDataDtoFromRedis("ds:recentStatistics:" + today);
        if (cachedChartData != null) {
            return cachedChartData;
        }
        return calculateRecentStatistics();
    }

    @Scheduled(cron = "${task.schedule.cron.dailyGetEachServiceStatistics}")
    public ChartDataDTO calculateRecentStatistics() {
        LocalDate today = LocalDate.now();
        Pageable pageable = PageRequest.of(0, 365);

        List<DailyStatisticVO> statistics = dailyStatisticJpa.findRecentStatistics(today, pageable);
        Collections.reverse(statistics);

        ChartDataDTO chartData = buildChartDataFromStatistics(statistics);
        saveChartDataDtoToRedis("ds:recentStatistics:" + today, chartData);
        return chartData;
    }

    private ChartDataDTO buildChartDataFromStatistics(List<DailyStatisticVO> statistics) {
        ChartDataDTO chartData = new ChartDataDTO();
        for (DailyStatisticVO stat : statistics) {
            chartData.addCreatedData("url", stat.getDsShortUrlCreated());
            chartData.addCreatedData("album", stat.getDsImgAlbumCreated());
            chartData.addCreatedData("image", stat.getDsImgCreated());
            chartData.addUsedData("url", stat.getDsShortUrlUsed());
            chartData.addUsedData("album", stat.getDsImgAlbumUsed());
            chartData.addUsedData("image", stat.getDsImgUsed());
        }
        return chartData;
    }

    private void saveChartDataDtoToRedis(String key, ChartDataDTO dto) {
        try {
            String json = objectMapper.writeValueAsString(dto);
            redisStringStringTemplate.opsForValue().set(key, json, TTL_DURATION);
        } catch (Exception e) {
            logger.error("Failed to save ChartDataDTO to Redis. Key: {}.", key, e);
        }
    }

    private ChartDataDTO getChartDataDtoFromRedis(String key) {
        try {
            String json = redisStringStringTemplate.opsForValue().get(key);
            if (json != null) {
                return objectMapper.readValue(json, ChartDataDTO.class);
            }
        } catch (Exception e) {
            logger.error("Failed to retrieve ChartDataDTO from Redis. Key: {}.", key, e);
        }
        return null;
    }

    // =============================================================
    // daily usage data statistics with Redis

    private void incrementAndSetTTL(String key) {
        Long newValue = redisStringIntegerTemplate.opsForValue().increment(key, 1);
        if (newValue != null && newValue == 1L) {
            redisStringIntegerTemplate.expire(key, TTL_DURATION);
        }
    }

    public void incrementShortUrlCreated() {
        String key = REDIS_KEY_STATISTIC_PREFIX + getCurrentDate() + ":shortUrlCreated";
        incrementAndSetTTL(key);
    }

    public void incrementShortUrlUsed() {
        String key = REDIS_KEY_STATISTIC_PREFIX + getCurrentDate() + ":shortUrlUsed";
        incrementAndSetTTL(key);
    }

    public void incrementImgCreated() {
        String key = REDIS_KEY_STATISTIC_PREFIX + getCurrentDate() + ":imgCreated";
        incrementAndSetTTL(key);
    }

    public void incrementImgUsed() {
        String key = REDIS_KEY_STATISTIC_PREFIX + getCurrentDate() + ":imgUsed";
        incrementAndSetTTL(key);
    }

    public void incrementImgAlbumCreated() {
        String key = REDIS_KEY_STATISTIC_PREFIX + getCurrentDate() + ":imgAlbumCreated";
        incrementAndSetTTL(key);
    }

    public void incrementImgAlbumUsed() {
        String key = REDIS_KEY_STATISTIC_PREFIX + getCurrentDate() + ":imgAlbumUsed";
        incrementAndSetTTL(key);
    }

    public void incrementCssJsMinified() {
        String key = REDIS_KEY_STATISTIC_PREFIX + getCurrentDate() + ":cssJsMinified";
        incrementAndSetTTL(key);
    }

    public void incrementImgToWebpUsed() {
        String key = REDIS_KEY_STATISTIC_PREFIX + getCurrentDate() + ":imgToWebpUsed";
        incrementAndSetTTL(key);
    }

    public Integer getStatisticOrDefault(String type) {
        String key = REDIS_KEY_STATISTIC_PREFIX + getCurrentDate() + ":" + type;
        Integer value = redisStringIntegerTemplate.opsForValue().get(key);
        return value != null ? value : Integer.valueOf(0);
    }

    public void clearStatisticsForDate() {
        String dateStr = getCurrentDate();
        redisStringIntegerTemplate.delete(REDIS_KEY_STATISTIC_PREFIX + dateStr + ":shortUrlCreated");
        redisStringIntegerTemplate.delete(REDIS_KEY_STATISTIC_PREFIX + dateStr + ":shortUrlUsed");
        redisStringIntegerTemplate.delete(REDIS_KEY_STATISTIC_PREFIX + dateStr + ":imgCreated");
        redisStringIntegerTemplate.delete(REDIS_KEY_STATISTIC_PREFIX + dateStr + ":imgUsed");
        redisStringIntegerTemplate.delete(REDIS_KEY_STATISTIC_PREFIX + dateStr + ":imgAlbumCreated");
        redisStringIntegerTemplate.delete(REDIS_KEY_STATISTIC_PREFIX + dateStr + ":imgAlbumUsed");
        redisStringIntegerTemplate.delete(REDIS_KEY_STATISTIC_PREFIX + dateStr + ":cssJsMinified");
        redisStringIntegerTemplate.delete(REDIS_KEY_STATISTIC_PREFIX + dateStr + ":imgToWebpUsed");
    }

    private static String getCurrentDate() {
        return LocalDate.now().toString();
    }

}