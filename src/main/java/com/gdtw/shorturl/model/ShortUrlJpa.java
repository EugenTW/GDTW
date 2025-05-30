package com.gdtw.shorturl.model;

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

}
