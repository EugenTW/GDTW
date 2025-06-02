package com.gdtw.general.service.scheduled;

import com.gdtw.dailystatistic.model.DailyStatisticJpa;
import com.gdtw.dailystatistic.model.DailyStatisticService;
import com.gdtw.dailystatistic.model.DailyStatisticVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class ScheduledStatService {

    private static final Logger logger = LoggerFactory.getLogger(ScheduledStatService.class);

    private final DailyStatisticService statisticService;
    private final DailyStatisticJpa dailyStatisticJpa;

    public ScheduledStatService(DailyStatisticService dailyStatisticService, DailyStatisticJpa dailyStatisticJpa) {
        this.statisticService = dailyStatisticService;
        this.dailyStatisticJpa = dailyStatisticJpa;
    }

    // Scheduled task to run daily
    @Scheduled(cron = "${task.schedule.cron.dailyStatisticService}")
    @Transactional
    public void collectAndSaveStatistics() {
        saveStatistics();
    }

    public void saveStatistics() {

        LocalDate currentDate = LocalDate.now();
        DailyStatisticVO statistic = dailyStatisticJpa.findByDsDate(currentDate);

        // Get statistics from Redis
        Integer shortUrlCreated = statisticService.getStatisticOrDefault("shortUrlCreated");
        Integer shortUrlUsed = statisticService.getStatisticOrDefault("shortUrlUsed");
        Integer imgCreated = statisticService.getStatisticOrDefault("imgCreated");
        Integer imgUsed = statisticService.getStatisticOrDefault("imgUsed");
        Integer imgAlbumCreated = statisticService.getStatisticOrDefault("imgAlbumCreated");
        Integer imgAlbumUsed = statisticService.getStatisticOrDefault("imgAlbumUsed");
        Integer cssJsMinified = statisticService.getStatisticOrDefault("cssJsMinified");
        Integer imgToWebpUsed = statisticService.getStatisticOrDefault("imgToWebpUsed");

        // Write statistics to MySQL
        if (statistic != null) {
            // If there is already data for the current day, merge the statistics
            statistic.setDsShortUrlCreated(statistic.getDsShortUrlCreated() + shortUrlCreated);
            statistic.setDsShortUrlUsed(statistic.getDsShortUrlUsed() + shortUrlUsed);
            statistic.setDsImgCreated(statistic.getDsImgCreated() + imgCreated);
            statistic.setDsImgUsed(statistic.getDsImgUsed() + imgUsed);
            statistic.setDsImgAlbumCreated(statistic.getDsImgAlbumCreated() + imgAlbumCreated);
            statistic.setDsImgAlbumUsed(statistic.getDsImgAlbumUsed() + imgAlbumUsed);
            statistic.setDsCssJsMinified(statistic.getDsCssJsMinified() + cssJsMinified);
            statistic.setDsImgToWebpUsed(statistic.getDsImgToWebpUsed() + imgToWebpUsed);
        } else {
            // If there is no data for the current day, create a new record
            statistic = new DailyStatisticVO();
            statistic.setDsDate(currentDate);
            statistic.setDsShortUrlCreated(shortUrlCreated);
            statistic.setDsShortUrlUsed(shortUrlUsed);
            statistic.setDsImgCreated(imgCreated);
            statistic.setDsImgUsed(imgUsed);
            statistic.setDsImgAlbumCreated(imgAlbumCreated);
            statistic.setDsImgAlbumUsed(imgAlbumUsed);
            statistic.setDsCssJsMinified(cssJsMinified);
            statistic.setDsImgToWebpUsed(imgToWebpUsed);
        }

        // Save or update data to MySQL
        dailyStatisticJpa.save(statistic);

        // Clear the Redis data for the current day
        statisticService.clearStatisticsForDate();
        logger.info("Sync 'Daily Statistic' to MySQL!");
    }

}
