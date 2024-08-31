package com.GDTW.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;

@Service
public class StatisticService {
    private final RedisTemplate<String, Integer> redisTemplate;
    private static final Duration TTL_DURATION = Duration.ofHours(36);

    public StatisticService(RedisTemplate<String, Integer> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    private String getCurrentDate() {
        return LocalDate.now().toString();
    }

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
        redisTemplate.delete("statistic:" + dateStr + ":vidCreated");
        redisTemplate.delete("statistic:" + dateStr + ":vidUsed");
    }
}
