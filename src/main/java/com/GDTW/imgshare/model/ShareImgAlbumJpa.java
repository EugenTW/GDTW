package com.GDTW.imgshare.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShareImgAlbumJpa extends JpaRepository<ShareImgAlbumVO, Integer> {

    boolean existsBySiaId(Integer siaId);

    boolean existsBySiaIdAndSiaStatusNot(Integer siaId, Byte siaStatus);

    Optional<ShareImgAlbumVO> findBySiaId(Integer siaId);

    List<ShareImgAlbumVO> findBySiaEndDateBeforeAndSiaStatus(LocalDate endDate, Byte status);

}
