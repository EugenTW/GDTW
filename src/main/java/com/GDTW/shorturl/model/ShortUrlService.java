package com.GDTW.shorturl.model;

import com.GDTW.service.IdEncoderDecoderService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ShortUrlService {

    @Autowired
    private ShortUrlJpa shortUrlJpa;
    // ==================================================================
    // Service methods


    // ==================================================================
    // Read-only methods

    // Determine whether the short URL is valid or not
    @Transactional(readOnly = true)
    public boolean isShortUrlIdExist(Integer suId) {
        return shortUrlJpa.existsBySuId(suId);
    }


    // ==================================================================
    // Writing methods

    // ==================================================================
    // Supporting methods

    public static String toEncodeSuId (Integer id){
        return IdEncoderDecoderService.encodeId(id);
    }

    public static Integer toDecodeSuId (String encodeSuId){
        return IdEncoderDecoderService.decodeId(encodeSuId);
    }
}
