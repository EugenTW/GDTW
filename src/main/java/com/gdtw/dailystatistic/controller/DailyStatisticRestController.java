package com.gdtw.dailystatistic.controller;

import com.gdtw.dailystatistic.model.ChartDataDTO;
import com.gdtw.dailystatistic.model.DailyStatisticService;
import com.gdtw.dailystatistic.model.TotalServiceStatisticsDTO;
import com.gdtw.general.service.ratelimiter.RateLimiterService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ds_api")
public class DailyStatisticRestController {

    private final DailyStatisticService dailyStatisticService;
    private final RateLimiterService rateLimiterService;

    public DailyStatisticRestController(DailyStatisticService dailyStatisticService, RateLimiterService rateLimiterService) {
        this.dailyStatisticService = dailyStatisticService;
        this.rateLimiterService = rateLimiterService;
    }

    @PostMapping("/total_service_statistics")
    public ResponseEntity<TotalServiceStatisticsDTO> getTotalServiceStatistics(HttpServletRequest originRequest) {

        String originalIp = originRequest.getHeader("X-Forwarded-For");
        rateLimiterService.checkGetDailyStatisticLimit(originalIp);

        TotalServiceStatisticsDTO statistics = dailyStatisticService.getTotalServiceStatistics();
        return ResponseEntity.ok(statistics);
    }

    @PostMapping("/recent_statistics")
    public ResponseEntity<ChartDataDTO> getRecentStatistics(HttpServletRequest originRequest) {

        String originalIp = originRequest.getHeader("X-Forwarded-For");
        rateLimiterService.checkGetDailyStatisticLimit(originalIp);

        ChartDataDTO chartData = dailyStatisticService.getRecentStatisticsForCharts();
        return ResponseEntity.ok(chartData);
    }

}
