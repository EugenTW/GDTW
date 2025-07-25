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

    @Transactional
    @Modifying
    @Query("UPDATE ShareImgVO s SET s.siStatus = 1 WHERE s.album.siaId IN :albumIds")
    int blockImagesByAlbumIds(@Param("albumIds") List<Integer> albumIds);

    @Transactional
    @Modifying
    @Query("UPDATE ShareImgVO s SET s.siStatus = 1 WHERE s.siStatus = 0 AND s.siReported > :reportedThreshold AND (s.siTotalVisited IS NOT NULL AND s.siReported * 1.0 / s.siTotalVisited >= :reportedProportion)")
    int blockReportedImages(@Param("reportedThreshold") int reportedThreshold, @Param("reportedProportion") double reportedProportion);

}
