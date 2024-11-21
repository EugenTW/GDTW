package com.GDTW.dailystatistic.controller;

import com.GDTW.dailystatistic.model.ChartDataDTO;
import com.GDTW.dailystatistic.model.DailyStatisticService;
import com.GDTW.dailystatistic.model.TotalServiceStatisticsDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/ds_api")
public class DailyStatisticRestController {

    private final DailyStatisticService dailyStatisticService;

    public DailyStatisticRestController(DailyStatisticService dailyStatisticService) {
        this.dailyStatisticService = dailyStatisticService;
    }

    @PostMapping("/total_service_statistics")
    public ResponseEntity<TotalServiceStatisticsDTO> getTotalServiceStatistics(@RequestBody(required = false) Map<String, Object> request) {
        TotalServiceStatisticsDTO statistics = dailyStatisticService.getTotalServiceStatistics();
        return ResponseEntity.ok(statistics);
    }

    @PostMapping("/recent_statistics")
    public ResponseEntity<ChartDataDTO> getRecentStatistics() {
        ChartDataDTO chartData = dailyStatisticService.getRecentStatisticsForCharts();
        return ResponseEntity.ok(chartData);
    }

}
