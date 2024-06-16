package com.GDTW.shorturl.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ShortUrlJpa extends JpaRepository<ShortUrlVO, Integer> {

    boolean existsById(Integer suId);

    @Query("FROM ShortUrlVO WHERE suId = :suId")
    ShortUrlVO findByuId(Integer suId);

}
