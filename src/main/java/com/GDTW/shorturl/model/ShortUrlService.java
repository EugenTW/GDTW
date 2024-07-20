package com.GDTW.shorturl.model;

import com.GDTW.service.IdEncoderDecoderService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Optional;

@Service
public class ShortUrlService {

    private final ShortUrlJpa shortUrlJpa;

    public ShortUrlService(ShortUrlJpa shortUrlJpa) {
        this.shortUrlJpa = shortUrlJpa;
    }
    // ==================================================================
    // Service methods

    @Transactional
    public String createNewShortUrl(String originalUrl, String originalIp) {
        Integer suId = recordOriginalUrl(originalUrl, originalIp);
        return encodeShortUrl(suId);
    }

    @Transactional
    public String getOriginalUrl(String shortUrl) {
        Integer suId = toDecodeSuId(shortUrl);
        if (!isShortUrlIdExist(suId)){return "na";}
        if (!isShortUrlValid(suId)){return "ban";}
        countShortUrlUsage(suId);
        return getOriginalUrl(suId);
    }


    // ==================================================================
    // Read-only methods

    @Transactional(readOnly = true)
    public boolean isShortUrlIdExist(Integer suId) {
        return shortUrlJpa.existsBySuId(suId);
    }

    @Transactional(readOnly = true)
    public boolean isShortUrlValid(Integer suId) {
        return shortUrlJpa.checkShortUrlStatus(suId);
    }

    @Transactional(readOnly = true)
    public boolean isShortUrlHavingUId(Integer suId) {
        return shortUrlJpa.checkShortUrlCreator(suId);
    }

    @Transactional(readOnly = true)
    public ShortUrlVO getAllDataOfShortUrl(Integer suId) {
        return shortUrlJpa.findBySuId(suId);
    }

    @Transactional(readOnly = true)
    public String getShortenUrl(Integer suId) {
        return shortUrlJpa.findSuShortenedUrlBySuId(suId);
    }

    @Transactional(readOnly = true)
    public String getOriginalUrl(Integer suId) {
        return shortUrlJpa.findOriginalUrlBySuId(suId);
    }

    // ==================================================================
    // Writing methods

    @Transactional
    public Integer recordOriginalUrl(String originalUrl, String originalIp) {
        ShortUrlVO shortUrl = new ShortUrlVO();
        shortUrl.setSuOriginalUrl(originalUrl);
        shortUrl.setSuCreatedIp(originalIp);
        shortUrl.setSuCreatedDate(new Date());
        shortUrl.setSuStatus(0);
        shortUrl.setSuTotalUsed(0);
        ShortUrlVO savedShortUrl = shortUrlJpa.save(shortUrl);
        return savedShortUrl.getSuId();
    }

    @Transactional
    public String encodeShortUrl(Integer suId) {
        Optional<ShortUrlVO> optionalShortUrl = shortUrlJpa.findById(suId);
        if (optionalShortUrl.isPresent()) {
            ShortUrlVO shortUrl = optionalShortUrl.get();
            String encodedUrl = toEncodeSuId(suId);
            shortUrl.setSuShortenedUrl(encodedUrl);
            shortUrlJpa.save(shortUrl);
            return encodedUrl;
        } else {
            throw new IllegalArgumentException("Invalid suId: " + suId);
        }
    }

    @Transactional
    public void countShortUrlUsage(Integer suId) {
        Optional<ShortUrlVO> optionalShortUrl = shortUrlJpa.findById(suId);
        if (optionalShortUrl.isPresent()) {
            ShortUrlVO shortUrl = optionalShortUrl.get();
            int usageCount = shortUrl.getSuTotalUsed() + 1;
            shortUrl.setSuTotalUsed(usageCount);
            shortUrlJpa.save(shortUrl);
        }
    }

    // ==================================================================
    // Supporting methods

    public static String toEncodeSuId(Integer id) {
        return IdEncoderDecoderService.encodeId(id);
    }

    public static Integer toDecodeSuId(String encodeSuId) {
        return IdEncoderDecoderService.decodeId(encodeSuId);
    }
}
