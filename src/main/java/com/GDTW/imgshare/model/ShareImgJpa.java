package com.GDTW.imgshare.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ShareImgJpa extends JpaRepository<ShareImgVO, Integer> {

    boolean existsBySiId(Integer siId);

    boolean existsBySiIdAndSiStatusNot(Integer siId, Byte siStatus);

    List<ShareImgVO> findByAlbum_SiaIdOrderBySiIdAsc(Integer siaId);

    List<ShareImgVO> findBySiEndDateBeforeAndSiStatus(LocalDate endDate, Byte status);

}
