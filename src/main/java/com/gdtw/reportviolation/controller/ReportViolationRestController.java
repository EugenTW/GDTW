package com.gdtw.reportviolation.controller;

import com.gdtw.reportviolation.model.ReportRequestDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/rv_api")
public class ReportViolationRestController {

    private static final String HEADER_X_FORWARDED_FOR = "X-Forwarded-For";

    @PostMapping("/send_report")
    public ResponseEntity<Map<String, String>> sendReport(
            @RequestBody ReportRequestDTO reportRequestDTO,
            HttpServletRequest request) {

        String originalIp = request.getHeader(HEADER_X_FORWARDED_FOR);
        int reportType = reportRequestDTO.getReportType();
        int reportReason = reportRequestDTO.getReportReason();
        String reportTargetUrl = reportRequestDTO.getTargetUrl();

        System.out.println("Report Target URL: " + reportTargetUrl);

        Map<String, String> response = new HashMap<>();

        return ResponseEntity.ok(response);
    }



}
