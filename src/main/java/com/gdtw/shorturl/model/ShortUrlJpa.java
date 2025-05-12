package com.gdtw.shorturl.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository
public interface ShortUrlJpa extends JpaRepository<ShortUrlVO, Integer> {

    boolean existsBySuId(Integer suId);

    @Query("SELECT new map(s.suId as suId, s.suSafe as suSafe, s.suOriginalUrl as suOriginalUrl) FROM ShortUrlVO s WHERE s.suId = :suId")
    Map<String, Object> findSuIdAndSuSafeBySuId(@Param("suId") Integer suId);

    @Query("SELECT CASE WHEN s.suStatus = 0 THEN TRUE ELSE FALSE END FROM ShortUrlVO s WHERE s.suId = :suId")
    boolean checkShortUrlStatus(@Param("suId") Integer suId);

}
