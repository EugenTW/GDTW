package com.gdtw.general.helper;

import com.gdtw.general.service.RedisService;
import com.gdtw.general.service.scheduled.ScheduledStatService;
import com.gdtw.imgshare.service.ImgShareService;
import com.gdtw.shorturl.service.ShortUrlService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class ShutdownListenerHelper implements ApplicationListener<ContextClosedEvent> {

    private static final Logger logger = LoggerFactory.getLogger(ShutdownListenerHelper.class);

    private final ScheduledStatService scheduledStatService;
    private final RedisService redisService;
    private final ShortUrlService shortUrlService;
    private final ImgShareService imgShareService;

    public ShutdownListenerHelper(ScheduledStatService scheduledTaskService, ShortUrlService shortUrlService, RedisService redisService, ImgShareService imgShareService) {
        this.scheduledStatService = scheduledTaskService;
        this.shortUrlService = shortUrlService;
        this.redisService = redisService;
        this.imgShareService = imgShareService;
    }

    @Override
    public void onApplicationEvent(@NonNull ContextClosedEvent event) {
        try {
            logger.info("Starting shutdown process...");

            // Save daily statistics
            scheduledStatService.saveStatistics();

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
            logger.error("Error during saving Redis statistic data before shutdown.", e);
        }
    }

}

