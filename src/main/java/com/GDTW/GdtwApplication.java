package com.GDTW;

import com.GDTW.general.service.RedisService;
import com.GDTW.general.service.SitemapService;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

@SpringBootApplication
@EnableScheduling
public class GdtwApplication {

    private static final Logger logger = LoggerFactory.getLogger(GdtwApplication.class);

    private final SitemapService sitemapService;
    private final RedisService redisService;

    public GdtwApplication(SitemapService sitemapService, RedisService redisService) {
        this.sitemapService = sitemapService;
        this.redisService = redisService;
    }

    public static void main(String[] args) {
        SpringApplication.run(GdtwApplication.class, args);
        logger.info("Web version: " + WebVersion.getWebVersion() + " and built at " + WebVersion.getBuildDate());
    }

    // Generate sitemap on application startup
    @PostConstruct
    public void onStartup() {
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("Asia/Taipei"));
            sitemapService.generateSitemap();
            boolean confirm = true;
            redisService.clearRedis(confirm);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

}
