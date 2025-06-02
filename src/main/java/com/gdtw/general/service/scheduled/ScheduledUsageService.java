package com.gdtw.general.service.scheduled;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ScheduledUsageService {

    private final Logger logger = LoggerFactory.getLogger(ScheduledUsageService.class);
    private final RedisTemplate<String, Integer> redisStringIntegerTemplate;
    private final ApplicationContext applicationContext;

    public ScheduledUsageService(
            RedisTemplate<String, Integer> redisTemplateInteger,
            ApplicationContext applicationContext
    ) {
        this.redisStringIntegerTemplate = redisTemplateInteger;
        this.applicationContext = applicationContext;
    }

    public void countServiceUsage(String servicePrefix, Integer id) {
        String redisKey = servicePrefix + id;
        redisStringIntegerTemplate.opsForValue().increment(redisKey, 1);
    }

    @Scheduled(cron = "${task.schedule.cron.shortUtlUsageStatisticService}")
    @Transactional
    public void countShortUrlUsage() {
        applicationContext.getBean("shortUrlService", com.gdtw.shorturl.model.ShortUrlService.class)
                .syncSuUsageToMySQL();
        logger.info("Sync 'Short URL' usage to MySQL!");
    }

    @Scheduled(cron = "${task.schedule.cron.albumUsageStatisticService}")
    @Transactional
    public void countAlbumUsage() {
        applicationContext.getBean("imgShareService", com.gdtw.imgshare.model.ImgShareService.class)
                .syncSiaUsageToMySQL();
        logger.info("Sync 'Album' usage to MySQL!");
    }

    @Scheduled(cron = "${task.schedule.cron.imageUsageStatisticService}")
    @Transactional
    public void countImageUsage() {
        applicationContext.getBean("imgShareService", com.gdtw.imgshare.model.ImgShareService.class)
                .syncSiUsageToMySQL();
        logger.info("Sync 'Image' usage to MySQL!");
    }

}
