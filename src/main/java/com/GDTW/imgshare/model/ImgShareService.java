package com.GDTW.imgshare.model;

import com.GDTW.general.service.ExtendedIdEncoderDecoderService;
import com.GDTW.general.service.IdEncoderDecoderService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Service
public class ImgShareService {

    private final ShareImgAlbumJpa shareImgAlbumJpa;
    private final ShareImgJpa shareImgJpa;
    private final RedisTemplate<String, String> redisTemplate;

    private static final Duration TTL_DURATION = Duration.ofHours(36);

    public ImgShareService(ShareImgAlbumJpa shareImgAlbumJpa, ShareImgJpa shareImgJpa, RedisTemplate<String, String> redisTemplate) {
        this.shareImgAlbumJpa = shareImgAlbumJpa;
        this.shareImgJpa = shareImgJpa;
        this.redisTemplate = redisTemplate;
    }

    // ==================================================================
    // Writing methods

    @Transactional(readOnly = true)
    public boolean isShareImageAlbumCodeValid (String code){
        Integer siaImageAlbumId = toDecodeSuId(code);
        return shareImgAlbumJpa.existsBySiaIdAndSiaStatusNot(siaImageAlbumId, (byte) 1);
    }

    @Transactional(readOnly = true)
    public boolean isShareImageCodeValid (String code){
        Integer siImageId = toDecodeSuId(code);
        return shareImgJpa.existsBySiIdAndSiStatusNot(siImageId, (byte) 1);
    }

    // ==================================================================
    // Reading methods


    // ==================================================================
    // Redis caching methods

    // ==================================================================
    // Supporting methods

    public static String toEncodeSuId(Integer id) {
        return ExtendedIdEncoderDecoderService.encodeExtendedId(id);
    }

    public static Integer toDecodeSuId(String encodeId) {
        return ExtendedIdEncoderDecoderService.decodeExtendedId(encodeId);
    }

}
