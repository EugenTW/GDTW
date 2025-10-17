package com.gdtw.shorturl.repository;

import com.gdtw.shorturl.model.ShortUrlVO;

import jakarta.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ShortUrlJpa extends JpaRepository<ShortUrlVO, Integer> {

    @Transactional
    @Modifying
    @Query("UPDATE ShortUrlVO s SET s.suReported = COALESCE(s.suReported, 0) + 1 WHERE s.suId = :suId AND (s.suStatus IS NULL OR s.suStatus = 0)")
    int incrementReportIfNotBlocked(@Param("suId") Integer suId);

    @Transactional
    @Modifying
    @Query("UPDATE ShortUrlVO s SET s.suStatus = 1 WHERE s.suStatus = 0 AND s.suReported > :reportedThreshold AND (s.suTotalUsed IS NOT NULL AND s.suReported * 1.0 / s.suTotalUsed >= :reportedProportion)")
    int blockReportedShortUrls(@Param("reportedThreshold") int reportedThreshold, @Param("reportedProportion") double reportedProportion);

}
