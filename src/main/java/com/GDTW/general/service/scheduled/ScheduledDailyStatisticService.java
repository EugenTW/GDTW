package com.GDTW.general.service.scheduled;

import com.GDTW.dailystatistic.model.DailyStatisticJpa;
import com.GDTW.dailystatistic.model.DailyStatisticService;
import com.GDTW.dailystatistic.model.DailyStatisticVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Date;

@Service
public class ScheduledDailyStatisticService {

    private static final Logger logger = LoggerFactory.getLogger(ScheduledDailyStatisticService.class);

    private final DailyStatisticService statisticService;
    private final DailyStatisticJpa dailyStatisticJpa;

    public ScheduledDailyStatisticService(DailyStatisticService dailyStatisticService, DailyStatisticJpa dailyStatisticJpa) {
        this.statisticService = dailyStatisticService;
        this.dailyStatisticJpa = dailyStatisticJpa;
    }

    // Scheduled task to run daily
    @Scheduled(cron = "${task.schedule.cron.dailyStatisticService}")
    @Transactional
    public void collectAndSaveStatistics() {
        saveStatistics();
        logger.info("Daily statistics is saved successfully and old date is removed.");
    }

    public void saveStatistics() {

        // Get statistics from Redis
        Integer shortUrlCreated = statisticService.getStatisticOrDefault("shortUrlCreated");
        Integer shortUrlUsed = statisticService.getStatisticOrDefault("shortUrlUsed");
        Integer imgCreated = statisticService.getStatisticOrDefault("imgCreated");
        Integer imgUsed = statisticService.getStatisticOrDefault("imgUsed");
        Integer imgAlbumCreated = statisticService.getStatisticOrDefault("imgAlbumCreated");
        Integer imgAlbumUsed = statisticService.getStatisticOrDefault("imgAlbumUsed");
        Integer vidCreated = statisticService.getStatisticOrDefault("vidCreated");
        Integer vidUsed = statisticService.getStatisticOrDefault("vidUsed");

        // Write statistics to MySQL
        Date currentDate = java.sql.Date.valueOf(LocalDate.now());
        DailyStatisticVO statistic = dailyStatisticJpa.findByDsDate(currentDate);

        if (statistic != null) {
            // If there is already data for the current day, merge the statistics
            statistic.setDsShortUrlCreated(statistic.getDsShortUrlCreated() + shortUrlCreated);
            statistic.setDsShortUrlUsed(statistic.getDsShortUrlUsed() + shortUrlUsed);
            statistic.setDsImgCreated(statistic.getDsImgCreated() + imgCreated);
            statistic.setDsImgUsed(statistic.getDsImgUsed() + imgUsed);
            statistic.setDsImgAlbumCreated(statistic.getDsImgAlbumCreated() + imgAlbumCreated);
            statistic.setDsImgAlbumUsed(statistic.getDsImgAlbumUsed() + imgAlbumUsed);
            statistic.setDsVidCreated(statistic.getDsVidCreated() + vidCreated);
            statistic.setDsVidUsed(statistic.getDsVidUsed() + vidUsed);
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
            statistic.setDsVidCreated(vidCreated);
            statistic.setDsVidUsed(vidUsed);
        }

        // Save or update data to MySQL
        dailyStatisticJpa.save(statistic);

        // Clear the Redis data for the current day
        statisticService.clearStatisticsForDate();
    }

}
