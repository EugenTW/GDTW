package com.GDTW.shorturl.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ShortUrlJpa extends JpaRepository<ShortUrlVO, Integer> {

    boolean existsBySuId(Integer suId);

    @Query("FROM ShortUrlVO WHERE suId = :suId")
    ShortUrlVO findBySuId(Integer suId);

    @Query("SELECT CASE WHEN s.user IS NOT NULL THEN TRUE ELSE FALSE END FROM ShortUrlVO s WHERE s.suId = :suId")
    boolean existsUIdBySuId(@Param("suId") Integer suId);

    @Query("SELECT CASE WHEN s.suStatus = 0 THEN TRUE ELSE FALSE END FROM ShortUrlVO s WHERE s.suId = :suId")
    boolean isShortUrlValid(@Param("suId") Integer suId);

}
