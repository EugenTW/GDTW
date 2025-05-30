package com.gdtw.reportviolation.model;

import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Service
public class ReportViolationService {

    private static final Logger logger = LoggerFactory.getLogger(ReportViolationService.class);
    private final ReportViolationJpa reportViolationJpa;
    private final RedisTemplate<String, String> redisStringStringTemplate;

    public ReportViolationService(ReportViolationJpa reportViolationJpa, RedisTemplate<String, String> redisStringStringTemplate) {
        this.reportViolationJpa = reportViolationJpa;
        this.redisStringStringTemplate = redisStringStringTemplate;
    }

    public Map<String, String> createViolationReport(ReportRequestDTO dto, String originalIp) {
        Map<String, String> result = new HashMap<>();

        if (!"localhost".equals(originalIp) && !"127.0.0.1".equals(originalIp) && !"0:0:0:0:0:0:0:1".equals(originalIp)) {
            String redisKey = "reportlimit:" + originalIp;
            Boolean already = redisStringStringTemplate.hasKey(redisKey);
            if (Boolean.TRUE.equals(already)) {
                result.put("reportStatus", "false");
                result.put("response", "您每分鐘只能舉報一次，請稍後再試！\nYou can only report once per minute, please try again later!");
                return result;
            }
        }

        try {
            saveReportViolationTransactional(dto, originalIp);

            if (!"localhost".equals(originalIp) && !"127.0.0.1".equals(originalIp) && !"0:0:0:0:0:0:0:1".equals(originalIp)) {
                String redisKey = "reportlimit:" + originalIp;
                redisStringStringTemplate.opsForValue().set(redisKey, "1", Duration.ofMinutes(1));
            }

            result.put("reportStatus", "true");
            result.put("response", "舉報送出成功，謝謝您的回報！\nReport submitted successfully. Thank you!");
        } catch (Exception e) {
            logger.error("Create Violation Report Error: ", e);
            result.put("reportStatus", "false");
            result.put("response", "伺服器錯誤，請稍後再試。\nServer error, please try again. (" + e.getClass().getSimpleName() + ")");
        }
        return result;
    }

    @Transactional
    public void saveReportViolationTransactional(ReportRequestDTO dto, String ip) {
        ReportViolationVO vo = new ReportViolationVO();
        vo.setVrIp(ip);
        vo.setVrReportType(dto.getReportType());
        vo.setVrReportTarget(dto.getTargetUrl());
        vo.setVrReportReason(dto.getReportReason());
        reportViolationJpa.save(vo);
    }


}
