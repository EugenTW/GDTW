package com.gdtw.config;

import com.gdtw.general.service.RedisService;
import com.gdtw.general.service.scheduled.ScheduledDailyStatisticService;
import com.gdtw.imgshare.model.ImgShareService;
import com.gdtw.shorturl.model.ShortUrlService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class ShutdownListener implements ApplicationListener<ContextClosedEvent> {

    private static final Logger logger = LoggerFactory.getLogger(ShutdownListener.class);

    private final ScheduledDailyStatisticService scheduledDailyStatisticService;
    private final RedisService redisService;
    private final ShortUrlService shortUrlService;
    private final ImgShareService imgShareService;

    public ShutdownListener(ScheduledDailyStatisticService scheduledTaskService, ShortUrlService shortUrlService, RedisService redisService, ImgShareService imgShareService) {
        this.scheduledDailyStatisticService = scheduledTaskService;
        this.shortUrlService = shortUrlService;
        this.redisService = redisService;
        this.imgShareService = imgShareService;
    }

    @Override
    public void onApplicationEvent(@NonNull ContextClosedEvent event) {
        try {
            logger.info("Starting shutdown process...");

            // Save daily statistics
            scheduledDailyStatisticService.saveStatistics();

            // Sync Redis 'Short URL' usage data to MySQL
            shortUrlService.syncSuUsageToMySQL();

            // Sync Redis 'Share Image' usage data to MySQL
            imgShareService.syncSiaUsageToMySQL();
            imgShareService.syncSiUsageToMySQL();

            // Clear Redis data
            boolean confirm = true;
            redisService.clearRedis(confirm);

            logger.info("All Redis statistic data are saved into MySQL. Shutdown process completed successfully.");
        } catch (Exception e) {
            logger.error("Error during saving Redis statistic data before shutdown", e);
        }
    }

}

