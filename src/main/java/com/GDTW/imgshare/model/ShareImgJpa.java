package com.GDTW.imgshare.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShareImgJpa extends JpaRepository<ShareImgVO, Integer> {

    boolean existsBySiIdAndSiStatusNot(Integer siId, Byte siStatus);

}
