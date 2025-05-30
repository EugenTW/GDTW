package com.gdtw.reportviolation.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportViolationJpa extends JpaRepository<ReportViolationVO, Integer> {

}
