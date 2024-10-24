package com.GDTW.config;

import com.GDTW.general.service.RedisService;
import com.GDTW.general.service.ScheduledTaskService;
import com.GDTW.shorturl.model.ShortUrlService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

@Component
public class ShutdownListener implements ApplicationListener<ContextClosedEvent> {

    private static final Logger logger = LoggerFactory.getLogger(ShutdownListener.class);

    private final ScheduledTaskService scheduledTaskService;
    private final ShortUrlService shortUrlService;
    private final RedisService redisService;

    public ShutdownListener(ScheduledTaskService scheduledTaskService, ShortUrlService shortUrlService, RedisService redisService) {
        this.scheduledTaskService = scheduledTaskService;
        this.shortUrlService = shortUrlService;
        this.redisService = redisService;
    }

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        try {
            logger.info("Starting shutdown process...");

            // Save daily statistics
            scheduledTaskService.saveStatistics();

            // Sync Redis short URL usage data to MySQL
            shortUrlService.syncUsageToMySQL();

            // Clear Redis data
            redisService.clearRedis();

            logger.info("All Redis data are saved in MySQL. Shutdown process completed successfully.");
        } catch (Exception e) {
            logger.error("Error during shutdown process: " + e.getMessage(), e);
        }
    }
}

