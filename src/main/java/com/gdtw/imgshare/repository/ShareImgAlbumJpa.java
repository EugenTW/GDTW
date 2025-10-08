package com.gdtw.imgshare.repository;

import com.gdtw.imgshare.model.ShareImgAlbumVO;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShareImgAlbumJpa extends JpaRepository<ShareImgAlbumVO, Integer> {

    Optional<ShareImgAlbumVO> findBySiaId(Integer siaId);

    List<ShareImgAlbumVO> findBySiaEndDateBeforeAndSiaStatus(LocalDate endDate, Byte status);

    @Query("SELECT s.siaId FROM ShareImgAlbumVO s WHERE s.siaStatus = 0 AND s.siaReported > :reportedThreshold AND (s.siaTotalVisited IS NOT NULL AND s.siaReported * 1.0 / s.siaTotalVisited >= :reportedProportion)")
    List<Integer> findBlockedAlbumIds(@Param("reportedThreshold") int reportedThreshold, @Param("reportedProportion") double reportedProportion);

    @Transactional
    @Modifying
    @Query("UPDATE ShareImgAlbumVO s SET s.siaStatus = 1 WHERE s.siaStatus = 0 AND s.siaReported > :reportedThreshold AND (s.siaTotalVisited IS NOT NULL AND s.siaReported * 1.0 / s.siaTotalVisited >= :reportedProportion)")
    int blockReportedAlbums(@Param("reportedThreshold") int reportedThreshold, @Param("reportedProportion") double reportedProportion);

}