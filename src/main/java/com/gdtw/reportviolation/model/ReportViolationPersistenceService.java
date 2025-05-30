package com.gdtw.reportviolation.model;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class ReportViolationPersistenceService {

    private final ReportViolationJpa reportViolationJpa;

    public ReportViolationPersistenceService(ReportViolationJpa reportViolationJpa) {
        this.reportViolationJpa = reportViolationJpa;
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
