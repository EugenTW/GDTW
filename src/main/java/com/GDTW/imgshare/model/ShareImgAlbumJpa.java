package com.GDTW.imgshare.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShareImgAlbumJpa extends JpaRepository<ShareImgAlbumVO, Integer> {

    boolean existsBySiaIdAndSiaStatusNot(Integer siaId, Byte siaStatus);

    Optional<ShareImgAlbumVO> findBySiaId(Integer siaId);

}
