package com.GDTW.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class StatisticService {
    private final RedisTemplate<String, Integer> redisTemplate;

    public StatisticService(RedisTemplate<String, Integer> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    private String getCurrentDate() {
        return LocalDate.now().toString();
    }

    public void incrementShortUrlCreated() {
        String key = "statistic:" + getCurrentDate() + ":shortUrlCreated";
        redisTemplate.opsForValue().increment(key, 1);
    }

    public void incrementShortUrlUsed() {
        String key = "statistic:" + getCurrentDate() + ":shortUrlUsed";
        redisTemplate.opsForValue().increment(key, 1);
    }

    public void incrementImgCreated() {
        String key = "statistic:" + getCurrentDate() + ":imgCreated";
        redisTemplate.opsForValue().increment(key, 1);
    }

    public void incrementImgUsed() {
        String key = "statistic:" + getCurrentDate() + ":imgUsed";
        redisTemplate.opsForValue().increment(key, 1);
    }

    public void incrementVidCreated() {
        String key = "statistic:" + getCurrentDate() + ":vidCreated";
        redisTemplate.opsForValue().increment(key, 1);
    }

    public void incrementVidUsed() {
        String key = "statistic:" + getCurrentDate() + ":vidUsed";
        redisTemplate.opsForValue().increment(key, 1);
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
        redisTemplate.delete("statistic:" + dateStr + ":vidCreated");
        redisTemplate.delete("statistic:" + dateStr + ":vidUsed");
    }
}
