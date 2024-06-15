package com.GDTW.shorturl.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShortUrlJpa extends JpaRepository<ShortUrlVO, Integer> {
    // Test develop

}
