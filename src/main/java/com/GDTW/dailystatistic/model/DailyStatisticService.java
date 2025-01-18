package com.GDTW.dailystatistic.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@Service
public class DailyStatisticService {

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
        TotalServiceStatisticsDTO savedResultInRedis = getTotalServiceStatisticsDtoFromRedis("ds:totalStatistics:" + today);
        if (savedResultInRedis != null) {
            return savedResultInRedis;
        }
        return calculateTotalServiceStatistics();
    }

    @Scheduled(cron = "${task.schedule.cron.dailyCalculateTotalServiceStatistics}")
    @Transactional(readOnly = true)
    public TotalServiceStatisticsDTO calculateTotalServiceStatistics() {
        LocalDate today = LocalDate.now();
        Date currentDate = Date.from(today.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Object[] result = dailyStatisticJpa.calculateSumsBeforeDate(currentDate);

        TotalServiceStatisticsDTO totalStatistics = new TotalServiceStatisticsDTO();
        if (result != null && result.length > 0) {
            Object[] row = (Object[]) result[0];
            totalStatistics.setTotalShortUrlsCreated(row[0] != null ? ((Number) row[0]).intValue() : 0);
            totalStatistics.setTotalShortUrlsUsed(row[1] != null ? ((Number) row[1]).intValue() : 0);
            totalStatistics.setTotalImagesCreated(row[2] != null ? ((Number) row[2]).intValue() : 0);
            totalStatistics.setTotalImagesVisited(row[3] != null ? ((Number) row[3]).intValue() : 0);
            totalStatistics.setTotalImageAlbumsCreated(row[4] != null ? ((Number) row[4]).intValue() : 0);
            totalStatistics.setTotalImageAlbumsVisited(row[5] != null ? ((Number) row[5]).intValue() : 0);
        }
        saveTotalServiceStatisticsDtoToRedis("ds:totalStatistics:" + today, totalStatistics);
        return totalStatistics;
    }

    private void saveTotalServiceStatisticsDtoToRedis(String key, TotalServiceStatisticsDTO dto) {
        try {
            String json = objectMapper.writeValueAsString(dto);
            redisStringStringTemplate.opsForValue().set(key, json, TTL_DURATION);
        } catch (Exception e) {
            logger.error("Failed to save TotalServiceStatisticsDTO to Redis" + e.getMessage());
        }
    }

    private TotalServiceStatisticsDTO getTotalServiceStatisticsDtoFromRedis(String key) {
        try {
            String json = redisStringStringTemplate.opsForValue().get(key);
            if (json != null) {
                return objectMapper.readValue(json, TotalServiceStatisticsDTO.class);
            }
        } catch (Exception e) {
            logger.error("Failed to retrieve TotalServiceStatisticsDTO from Redis" + e.getMessage());
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
    @Transactional(readOnly = true)
    public ChartDataDTO calculateRecentStatistics() {
        LocalDate today = LocalDate.now();
        LocalDate currentDate = LocalDate.now();
        Pageable pageable = PageRequest.of(0, 360);

        List<DailyStatisticVO> statistics = dailyStatisticJpa.findRecentStatistics(currentDate, pageable);
        Collections.reverse(statistics);

        ChartDataDTO chartData = new ChartDataDTO();
        for (DailyStatisticVO stat : statistics) {
            chartData.addCreatedData("url", stat.getDsShortUrlCreated());
            chartData.addCreatedData("album", stat.getDsImgAlbumCreated());
            chartData.addCreatedData("image", stat.getDsImgCreated());
            chartData.addUsedData("url", stat.getDsShortUrlUsed());
            chartData.addUsedData("album", stat.getDsImgAlbumUsed());
            chartData.addUsedData("image", stat.getDsImgUsed());
        }
        saveChartDataDtoToRedis("ds:recentStatistics:" + today, chartData);
        return chartData;
    }


    private void saveChartDataDtoToRedis(String key, ChartDataDTO dto) {
        try {
            String json = objectMapper.writeValueAsString(dto);
            redisStringStringTemplate.opsForValue().set(key, json, TTL_DURATION);
        } catch (Exception e) {
            logger.error("Failed to save ChartDataDTO to Redis. Key: {}, Error: {}", key, e.getMessage());
        }
    }

    private ChartDataDTO getChartDataDtoFromRedis(String key) {
        try {
            String json = redisStringStringTemplate.opsForValue().get(key);
            if (json != null) {
                return objectMapper.readValue(json, ChartDataDTO.class);
            }
        } catch (Exception e) {
            logger.error("Failed to retrieve ChartDataDTO from Redis. Key: {}, Error: {}", key, e.getMessage());
        }
        return null;
    }

    // =============================================================
    // daily usage data statistics with Redis

    private void incrementAndSetTTL(String key) {
        redisStringIntegerTemplate.opsForValue().increment(key, 1);
        redisStringIntegerTemplate.expire(key, TTL_DURATION);
    }

    public void incrementShortUrlCreated() {
        String key = "statistic:" + getCurrentDate() + ":shortUrlCreated";
        incrementAndSetTTL(key);
    }

    public void incrementShortUrlUsed() {
        String key = "statistic:" + getCurrentDate() + ":shortUrlUsed";
        incrementAndSetTTL(key);
    }

    public void incrementImgCreated() {
        String key = "statistic:" + getCurrentDate() + ":imgCreated";
        incrementAndSetTTL(key);
    }

    public void incrementImgUsed() {
        String key = "statistic:" + getCurrentDate() + ":imgUsed";
        incrementAndSetTTL(key);
    }

    public void incrementImgAlbumCreated() {
        String key = "statistic:" + getCurrentDate() + ":imgAlbumCreated";
        incrementAndSetTTL(key);
    }

    public void incrementImgAlbumUsed() {
        String key = "statistic:" + getCurrentDate() + ":imgAlbumUsed";
        incrementAndSetTTL(key);
    }

    public void incrementVidCreated() {
        String key = "statistic:" + getCurrentDate() + ":vidCreated";
        incrementAndSetTTL(key);
    }

    public void incrementVidUsed() {
        String key = "statistic:" + getCurrentDate() + ":vidUsed";
        incrementAndSetTTL(key);
    }

    public Integer getStatisticOrDefault(String type) {
        String key = "statistic:" + getCurrentDate() + ":" + type;
        return redisStringIntegerTemplate.opsForValue().get(key) != null ? redisStringIntegerTemplate.opsForValue().get(key) : Integer.valueOf(0);
    }

    public void clearStatisticsForDate() {
        String dateStr = LocalDate.now().toString();
        redisStringIntegerTemplate.delete("statistic:" + dateStr + ":shortUrlCreated");
        redisStringIntegerTemplate.delete("statistic:" + dateStr + ":shortUrlUsed");
        redisStringIntegerTemplate.delete("statistic:" + dateStr + ":imgCreated");
        redisStringIntegerTemplate.delete("statistic:" + dateStr + ":imgUsed");
        redisStringIntegerTemplate.delete("statistic:" + dateStr + ":imgAlbumCreated");
        redisStringIntegerTemplate.delete("statistic:" + dateStr + ":imgAlbumUsed");
        redisStringIntegerTemplate.delete("statistic:" + dateStr + ":vidCreated");
        redisStringIntegerTemplate.delete("statistic:" + dateStr + ":vidUsed");
    }

    private static String getCurrentDate() {
        return LocalDate.now().toString();
    }

}