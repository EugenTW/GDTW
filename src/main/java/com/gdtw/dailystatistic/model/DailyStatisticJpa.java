package com.gdtw.dailystatistic.model;

import com.gdtw.dailystatistic.dto.DailyStatisticVO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DailyStatisticJpa extends JpaRepository<DailyStatisticVO, Integer> {

    DailyStatisticVO findByDsDate(LocalDate dsDate);

    @Query("SELECT SUM(dsShortUrlCreated), SUM(dsShortUrlUsed), SUM(dsImgCreated), SUM(dsImgUsed), SUM(dsImgAlbumCreated), SUM(dsImgAlbumUsed), COUNT(*), SUM(dsCssJsMinified), SUM(dsImgToWebpUsed) FROM DailyStatisticVO WHERE dsDate < :currentDate")
    Object[] calculateSumsBeforeDate(@Param("currentDate") LocalDate currentDate);

    @Query("SELECT d FROM DailyStatisticVO d WHERE d.dsDate < :currentDate ORDER BY d.dsDate DESC")
    List<DailyStatisticVO> findRecentStatistics(@Param("currentDate") LocalDate currentDate, Pageable pageable);

}
