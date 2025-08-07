package com.gdtw.reportviolation.service;

import com.gdtw.reportviolation.model.ReportRequestDTO;
import com.gdtw.reportviolation.repository.ReportViolationJpa;
import com.gdtw.reportviolation.model.ReportViolationVO;
import org.springframework.stereotype.Service;

@Service
public class ReportViolationPersistenceService {

    private final ReportViolationJpa reportViolationJpa;

    public ReportViolationPersistenceService(ReportViolationJpa reportViolationJpa) {
        this.reportViolationJpa = reportViolationJpa;
    }

    public void saveReportViolationTransactional(ReportRequestDTO dto, String ip) {
        ReportViolationVO vo = new ReportViolationVO();
        vo.setVrIp(ip);
        vo.setVrReportType(dto.getReportType());
        vo.setVrReportTarget(dto.getTargetUrl());
        vo.setVrReportReason(dto.getReportReason());
        reportViolationJpa.save(vo);
    }

}
