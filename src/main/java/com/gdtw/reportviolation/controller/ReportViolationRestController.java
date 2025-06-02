package com.gdtw.reportviolation.controller;

import com.gdtw.general.helper.ratelimiter.RateLimiterHelper;
import com.gdtw.reportviolation.dto.ReportRequestDTO;
import com.gdtw.reportviolation.model.ReportViolationService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;


@RestController
@RequestMapping("/rv_api")
public class ReportViolationRestController {

    private final RateLimiterHelper rateLimiterService;
    private final ReportViolationService reportViolationService;

    private static final String HEADER_X_FORWARDED_FOR = "X-Forwarded-For";

    public ReportViolationRestController(RateLimiterHelper rateLimiterService, ReportViolationService reportViolationService) {
        this.rateLimiterService = rateLimiterService;
        this.reportViolationService = reportViolationService;
    }

    @PostMapping("/send_report")
    public ResponseEntity<Map<String, String>> sendReport(
            @RequestBody ReportRequestDTO reportRequestDTO,
            HttpServletRequest request) {

        String originalIp = request.getHeader(HEADER_X_FORWARDED_FOR);
        rateLimiterService.checkReportViolationLimit(originalIp);

        Map<String, String> response = reportViolationService.createViolationReport (reportRequestDTO, originalIp);

        return ResponseEntity.ok(response);
    }

}
