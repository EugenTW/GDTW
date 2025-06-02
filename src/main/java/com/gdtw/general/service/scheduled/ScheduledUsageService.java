package com.gdtw.general.service.scheduled;

import com.gdtw.imgshare.model.ImgShareService;
import com.gdtw.shorturl.model.ShortUrlService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class ScheduledUsageService {

    private final Logger logger = LoggerFactory.getLogger(ScheduledUsageService.class);
    private final RedisTemplate<String,Integer> redisStringIntegerTemplate;
    private final ShortUrlService shortUrlService;
    private final ImgShareService imgShareService;

    public ScheduledUsageService(RedisTemplate<String, Integer> redisTemplateInteger, ShortUrlService shortUrlService, ImgShareService imgShareService) {
        this.redisStringIntegerTemplate = redisTemplateInteger;
        this.shortUrlService = shortUrlService;
        this.imgShareService = imgShareService;
    }

    public void countServiceUsage(String servicePrefix, Integer id) {
        String redisKey = servicePrefix + id;
        redisStringIntegerTemplate.opsForValue().increment(redisKey, 1);
    }

    @Scheduled(cron = "${task.schedule.cron.shortUtlUsageStatisticService}")
    @Transactional
    public void countShortUrlUsage(){
        shortUrlService.syncSuUsageToMySQL();
        logger.info("Sync 'Short URL' usage to MySQL!");
    }

    @Scheduled(cron = "${task.schedule.cron.albumUsageStatisticService}")
    @Transactional
    public void countAlbumUsage(){
        imgShareService.syncSiaUsageToMySQL();
        logger.info("Sync 'Album' usage to MySQL!");
    }

    @Scheduled(cron = "${task.schedule.cron.imageUsageStatisticService}")
    @Transactional
    public void countImageUsage(){
        imgShareService.syncSiUsageToMySQL();
        logger.info("Sync 'Image' usage to MySQL!");
    }

}
