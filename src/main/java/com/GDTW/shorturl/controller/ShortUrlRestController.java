package com.GDTW.shorturl.controller;

import com.GDTW.dailystatistic.service.DailyStatisticService;
import com.GDTW.general.exception.ShortUrlBannedException;
import com.GDTW.general.exception.ShortUrlNotFoundException;
import com.GDTW.general.service.RateLimiterService;
import com.GDTW.general.service.safebrowsing4.SafeBrowsingV4Service;
import com.GDTW.shorturl.model.*;
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
    private final ShortUrlService shortUrlService;
    private final DailyStatisticService statisticService;
    private final SafeBrowsingV4Service safeBrowsingService;
    private final RateLimiterService rateLimiterService;

    public ShortUrlRestController(ShortUrlService shortUrlService, DailyStatisticService statisticService, SafeBrowsingV4Service safeBrowsingService, RateLimiterService rateLimiterService) {
        this.shortUrlService = shortUrlService;
        this.statisticService = statisticService;
        this.safeBrowsingService = safeBrowsingService;
        this.rateLimiterService = rateLimiterService;
    }

    @Value("${app.baseUrl}")
    private String baseUrl;

    @PostMapping("/create_new_short_url")
    public ResponseEntity<ReturnCreatedShortUrlDTO> createNewShortUrl(@RequestBody CreateShortUrlRequestDTO shortUrlRequest, HttpServletRequest request) {

        String originalUrl = shortUrlRequest.getOriginalUrl();
        String originalIp = request.getHeader("X-Forwarded-For");
        rateLimiterService.checkCreateShortUrlLimit(originalIp);

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
            logger.error("Failed to create new shortUrl due to the web server error. The failed url was: '" + originalUrl + "'.");
            ReturnCreatedShortUrlDTO errorResponse = new ReturnCreatedShortUrlDTO(null, null, "內部伺服器錯誤!請等待站方維修! Internal server error! Please wait for the site to be fixed!");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/get_original_url")
    public ResponseEntity<ReturnOriginalUrlDTO> getOriginalUrl(@RequestBody GetOriginalUrlDTO codeRequest, HttpServletRequest request) {

        String originalIp = request.getHeader("X-Forwarded-For");
        rateLimiterService.checkGetOriginalUrlLimit(originalIp);
        String code = codeRequest.getCode();

        if (code.equalsIgnoreCase("short_url_redirection")) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ReturnOriginalUrlDTO(null, null, "無原始網址! No original URL!"));
        }

        try {
            Map.Entry<String, String> result = shortUrlService.getOriginalUrl(code);
            statisticService.incrementShortUrlUsed();
            return ResponseEntity.ok(new ReturnOriginalUrlDTO(result.getKey(), result.getValue(), null));
        } catch (ShortUrlNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ReturnOriginalUrlDTO(null, null, e.getMessage()));
        } catch (ShortUrlBannedException e) {
            return ResponseEntity.status(HttpStatus.GONE)
                    .body(new ReturnOriginalUrlDTO(null, null, e.getMessage()));
        } catch (Exception e) {
            logger.error("Invalid code input or internal server error. The input code was: '" + code + "'. \n" + e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ReturnOriginalUrlDTO(null, null, "內部伺服器錯誤! Internal Server Error!"));
        }
    }

}


