package com.GDTW.shorturl.controller;

import com.GDTW.dailystatistic.model.DailyStatisticService;
import com.GDTW.safebrowsing4.service.SafeBrowsingV4Service;
import com.GDTW.shorturl.model.*;
import com.google.common.util.concurrent.RateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/su_api")
public class ShortUrlRestController {
    private static final Logger logger = LoggerFactory.getLogger(ShortUrlRestController.class);
    private final RateLimiter CreatShortUrlRateLimiter = RateLimiter.create(60.0); // 60 requests per second
    private final RateLimiter GetOriginalUrlRateLimiter = RateLimiter.create(600.0); // 600 requests per second
    private final ShortUrlService shortUrlService;
    private final DailyStatisticService statisticService;
    private final SafeBrowsingV4Service safeBrowsingService;

    public ShortUrlRestController(ShortUrlService shortUrlService, DailyStatisticService statisticService, SafeBrowsingV4Service safeBrowsingService) {
        this.shortUrlService = shortUrlService;
        this.statisticService = statisticService;
        this.safeBrowsingService = safeBrowsingService;
    }

    @Value("${app.baseUrl}")
    private String baseUrl;

    @PostMapping("/create_new_short_url")
    public ResponseEntity<ReturnCreatedShortUrlDTO> createNewShortUrl(@RequestBody CreateShortUrlRequestDTO shortUrlRequest, HttpServletRequest request) {

        if (!CreatShortUrlRateLimiter.tryAcquire()) {
            ReturnCreatedShortUrlDTO errorResponse = new ReturnCreatedShortUrlDTO(null, null, "請求過於頻繁! 請稍後再試! Too many requests! Please try again later.");
            logger.warn("Short Url creation limit exceeded.");
            return ResponseEntity.status(429).body(errorResponse);
        }

        String originalUrl = shortUrlRequest.getOriginalUrl();
        String originalIp = request.getHeader("X-Forwarded-For");

        try {
            if (originalIp == null || originalIp.isEmpty()) {
                originalIp = request.getRemoteAddr();
            } else {
                originalIp = originalIp.split(",")[0];
            }

            String safeUrlResult = safeBrowsingService.checkUrlSafety(originalUrl);
            String shortUrl = shortUrlService.createNewShortUrl(originalUrl, originalIp, safeUrlResult);
            if (shortUrl != null) {
                String fullShortUrl = baseUrl + shortUrl;
                statisticService.incrementShortUrlCreated();

                // Create success response
                ReturnCreatedShortUrlDTO response = new ReturnCreatedShortUrlDTO(fullShortUrl, safeUrlResult, null);
                return ResponseEntity.ok(response);
            } else {
                // Log the error and return error response as JSON
                logger.error("Failed to create new shortUrl on MySQL. The failed url was: '" + originalUrl + "'.");
                ReturnCreatedShortUrlDTO errorResponse = new ReturnCreatedShortUrlDTO(null, safeUrlResult, "短網址建立失敗!請稍後再次嘗試! The short URL creation failed! Please try again later!");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }

        } catch (Exception e) {
            // Log the error and return error response as JSON
            logger.error("Failed to create new shortUrl due to the web server error. The failed url was: '" + originalUrl + "'.");
            ReturnCreatedShortUrlDTO errorResponse = new ReturnCreatedShortUrlDTO(null, null, "內部伺服器錯誤!請等待站方維修! Internal server error! Please wait for the site to be fixed!");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/get_original_url")
    public ResponseEntity<ReturnOriginalUrlDTO> getOriginalUrl(@RequestBody GetOriginalUrlDTO codeRequest) {

        if (!GetOriginalUrlRateLimiter.tryAcquire()) {
            ReturnOriginalUrlDTO errorResponse = new ReturnOriginalUrlDTO(null, null, "請求過於頻繁! 請稍後再試! Too many requests! Please try again later.");
            logger.warn("Original Url request limit exceeded.");
            return ResponseEntity.status(429).body(errorResponse);
        }

        try {
            String code = codeRequest.getCode();
            Map.Entry<String, String> result = shortUrlService.getOriginalUrl(code);
            String originalUrl = result.getKey();
            String originalUrlSafe = result.getValue();

            if (originalUrl == null || originalUrl.equals("na")) {
                ReturnOriginalUrlDTO errorResponse = new ReturnOriginalUrlDTO(null, null, "此短網址尚未建立! Original url not found!");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            } else if (originalUrl.equals("ban")) {
                ReturnOriginalUrlDTO errorResponse = new ReturnOriginalUrlDTO(null, null, "此短網址已失效! The short url is banned.");
                return ResponseEntity.status(HttpStatus.GONE).body(errorResponse);
            } else {
                ReturnOriginalUrlDTO response = new ReturnOriginalUrlDTO(originalUrl, originalUrlSafe, null);
                statisticService.incrementShortUrlUsed();
                return ResponseEntity.ok(response);
            }
        } catch (Exception e) {
            logger.error("Failed to return original url due to the internal server error.");
            ReturnOriginalUrlDTO errorResponse = new ReturnOriginalUrlDTO(null, null, "內部伺服器錯誤! Internal server error!");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

}
