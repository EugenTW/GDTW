package com.GDTW.dailystatistic.model;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface DailyStatisticJpa  extends JpaRepository<DailyStatisticVO, Integer> {

    DailyStatisticVO findByDsDate(Date dsDate);

    @Query("SELECT SUM(dsShortUrlCreated), SUM(dsShortUrlUsed), SUM(dsImgCreated), SUM(dsImgUsed), SUM(dsImgAlbumCreated), SUM(dsImgAlbumUsed) FROM DailyStatisticVO WHERE dsDate < :currentDate")
    Object[] calculateSumsBeforeDate(@Param("currentDate") Date currentDate);

    @Query("SELECT d FROM DailyStatisticVO d WHERE d.dsDate < :currentDate ORDER BY d.dsDate DESC")
    List<DailyStatisticVO> findRecentStatistics(@Param("currentDate") Date currentDate, Pageable pageable);


}
