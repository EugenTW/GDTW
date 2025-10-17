package com.gdtw.reportviolation.repository;

import com.gdtw.reportviolation.model.ReportViolationVO;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportViolationJpa extends JpaRepository<ReportViolationVO, Integer> {

}
