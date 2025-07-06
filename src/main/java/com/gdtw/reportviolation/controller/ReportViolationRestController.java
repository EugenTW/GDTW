package com.gdtw.reportviolation.controller;

import com.gdtw.general.helper.ratelimiter.RateLimiterHelper;
import com.gdtw.reportviolation.dto.ReportRequestDTO;
import com.gdtw.reportviolation.model.ReportViolationService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
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
        if (originalIp == null || originalIp.isBlank()) {
            originalIp = request.getRemoteAddr();
        }

        rateLimiterService.checkReportViolationLimit(originalIp);

        try {
            Map<String, String> response = reportViolationService.createViolationReport(reportRequestDTO, originalIp);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Throwable throwableError = e;
            Map<String, String> response = new HashMap<>();
            while (throwableError != null) {
                if (throwableError instanceof DataIntegrityViolationException
                        || throwableError instanceof org.hibernate.exception.ConstraintViolationException
                        || throwableError instanceof UnexpectedRollbackException) {
                    response.put("reportStatus", "false");
                    response.put("response", "您已對此資源舉報過，不能重複舉報。<br> You have already reported this resource and cannot report again.");
                    return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
                }
                throwableError = throwableError.getCause();
            }
            response.put("reportStatus", "false");
            response.put("response", "伺服器錯誤，請稍後再試。<br> Server error, please try again. (" + e.getClass().getSimpleName() + ")");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

}
