package com.gdtw.imgshare.model;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ShareImgJpa extends JpaRepository<ShareImgVO, Integer> {

    List<ShareImgVO> findByAlbum_SiaIdOrderBySiIdAsc(Integer siaId);

    List<ShareImgVO> findBySiEndDateBeforeAndSiStatus(LocalDate endDate, Byte status);

    @Modifying
    @Transactional
    @Query("UPDATE ShareImgVO s SET s.siReported = COALESCE(s.siReported, 0) + 1 WHERE s.siId = :siId AND (s.siStatus IS NULL OR s.siStatus = 0)")
    int incrementReportIfNotBlocked(@Param("siId") Integer siId);

}
