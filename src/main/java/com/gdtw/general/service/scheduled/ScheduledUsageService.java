package com.gdtw.general.service.scheduled;

import com.gdtw.imgshare.service.ImgShareService;
import com.gdtw.shorturl.service.ShortUrlService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ScheduledUsageService {

    private final Logger logger = LoggerFactory.getLogger(ScheduledUsageService.class);
    private final ShortUrlService shortUrlService;
    private final ImgShareService imgShareService;

    public ScheduledUsageService(ShortUrlService shortUrlService, ImgShareService imgShareService) {
        this.shortUrlService = shortUrlService;
        this.imgShareService = imgShareService;
    }

    @Scheduled(cron = "${task.schedule.cron.shortUtlUsageStatisticService}")
    @Transactional
    public void countShortUrlUsage() {
        shortUrlService.syncSuUsageToMySQL();
        logger.info("Sync 'Short URL' usage to MySQL!");
    }

    @Scheduled(cron = "${task.schedule.cron.albumUsageStatisticService}")
    @Transactional
    public void countAlbumUsage() {
        imgShareService.syncSiaUsageToMySQL();
        logger.info("Sync 'Album' usage to MySQL!");
    }

    @Scheduled(cron = "${task.schedule.cron.imageUsageStatisticService}")
    @Transactional
    public void countImageUsage() {
        imgShareService.syncSiUsageToMySQL();
        logger.info("Sync 'Image' usage to MySQL!");
    }

}
