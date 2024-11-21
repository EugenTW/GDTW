package com.GDTW.dailystatistic.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.util.*;

@Service
public class DailyStatisticService {

    private static final Logger logger = LoggerFactory.getLogger(DailyStatisticService.class);

    private final RedisTemplate<String, Integer> redisTemplate;
    private static final Duration TTL_DURATION = Duration.ofHours(25);
    private final DailyStatisticJpa dailyStatisticJpa;

    public DailyStatisticService(RedisTemplate<String, Integer> redisTemplate, DailyStatisticJpa dailyStatisticJpa) {
        this.redisTemplate = redisTemplate;
        this.dailyStatisticJpa = dailyStatisticJpa;
    }


    public TotalServiceStatisticsDTO getTotalServiceStatistics() {
        LocalDate today = LocalDate.now();

        Integer shortUrlsCreated = getValueFromRedis("daily_report:suc:" + today);
        Integer shortUrlsUsed = getValueFromRedis("daily_report:suu:" + today);
        Integer imagesCreated = getValueFromRedis("daily_report:ic:" + today);
        Integer imagesVisited = getValueFromRedis("daily_report:iv:" + today);
        Integer imageAlbumsCreated = getValueFromRedis("daily_report:iac:" + today);
        Integer imageAlbumsVisited = getValueFromRedis("daily_report:iav:" + today);

        if (shortUrlsCreated != null && shortUrlsUsed != null &&
                imagesCreated != null && imagesVisited != null &&
                imageAlbumsCreated != null && imageAlbumsVisited != null) {

            TotalServiceStatisticsDTO totalStatistics = new TotalServiceStatisticsDTO();
            totalStatistics.setTotalShortUrlsCreated(shortUrlsCreated);
            totalStatistics.setTotalShortUrlsUsed(shortUrlsUsed);
            totalStatistics.setTotalImagesCreated(imagesCreated);
            totalStatistics.setTotalImagesVisited(imagesVisited);
            totalStatistics.setTotalImageAlbumsCreated(imageAlbumsCreated);
            totalStatistics.setTotalImageAlbumsVisited(imageAlbumsVisited);

            return totalStatistics;
        }

        return calculateTotalServiceStatistics();
    }

    @Scheduled(cron = "${task.schedule.cron.dailyCalculateTotalServiceStatistics}")
    @Transactional(readOnly = true)
    public TotalServiceStatisticsDTO calculateTotalServiceStatistics() {
        Date currentDate = new Date();
        Object[] result = dailyStatisticJpa.calculateSumsBeforeDate(currentDate);

        TotalServiceStatisticsDTO totalStatistics = new TotalServiceStatisticsDTO();
        if (result != null && result.length > 0) {
            Object[] row = (Object[]) result[0]; // 解開數據
            totalStatistics.setTotalShortUrlsCreated(row[0] != null ? ((Number) row[0]).intValue() : 0);
            totalStatistics.setTotalShortUrlsUsed(row[1] != null ? ((Number) row[1]).intValue() : 0);
            totalStatistics.setTotalImagesCreated(row[2] != null ? ((Number) row[2]).intValue() : 0);
            totalStatistics.setTotalImagesVisited(row[3] != null ? ((Number) row[3]).intValue() : 0);
            totalStatistics.setTotalImageAlbumsCreated(row[4] != null ? ((Number) row[4]).intValue() : 0);
            totalStatistics.setTotalImageAlbumsVisited(row[5] != null ? ((Number) row[5]).intValue() : 0);
        }

        return totalStatistics;
    }



    // ===================================================
    // Daily statistic service with Redis

    private void incrementAndSetTTL(String key) {
        redisTemplate.opsForValue().increment(key, 1);
        redisTemplate.expire(key, TTL_DURATION);
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

    public Integer getStatistic(String type) {
        String key = "statistic:" + getCurrentDate() + ":" + type;
        return redisTemplate.opsForValue().get(key);
    }

    public Integer getStatisticOrDefault(String type) {
        String key = "statistic:" + getCurrentDate() + ":" + type;
        return redisTemplate.opsForValue().get(key) != null ? redisTemplate.opsForValue().get(key) : 0;
    }

    public void clearStatisticsForDate(LocalDate date) {
        String dateStr = date.toString();

        redisTemplate.delete("statistic:" + dateStr + ":shortUrlCreated");
        redisTemplate.delete("statistic:" + dateStr + ":shortUrlUsed");
        redisTemplate.delete("statistic:" + dateStr + ":imgCreated");
        redisTemplate.delete("statistic:" + dateStr + ":imgUsed");
        redisTemplate.delete("statistic:" + dateStr + ":imgAlbumCreated");
        redisTemplate.delete("statistic:" + dateStr + ":imgAlbumUsed");
        redisTemplate.delete("statistic:" + dateStr + ":vidCreated");
        redisTemplate.delete("statistic:" + dateStr + ":vidUsed");
    }

    // ===================================================
    private Integer getValueFromRedis(String key) {
        Object value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            return null;
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String getCurrentDate() {
        return LocalDate.now().toString();
    }



//==========================
@Scheduled(cron = "${task.schedule.cron.dailyGetEachServiceStatistics}")
    @Transactional(readOnly = true)
    public ChartDataDTO calculateRecentStatistics() {

        Date today = new Date();
        Pageable pageable = PageRequest.of(0, 365);
        List<DailyStatisticVO> statistics = dailyStatisticJpa.findRecentStatistics(today, pageable);
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

    public ChartDataDTO getRecentStatisticsForCharts() {

        ChartDataDTO chartData = calculateRecentStatistics();

        return chartData;
    }









}