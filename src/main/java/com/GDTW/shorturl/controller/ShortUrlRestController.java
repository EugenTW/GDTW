package com.GDTW.shorturl.controller;

import com.GDTW.general.service.StatisticService;
import com.GDTW.safebrowing.service.SafeBrowsingService;
import com.GDTW.shorturl.model.CreateShortUrlRequestDTO;
import com.GDTW.shorturl.model.GetOriginalUrlDTO;
import com.GDTW.shorturl.model.ShortUrlService;
import com.GDTW.shorturl.model.ReturnCreatedShortUrlDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/su_api")
public class ShortUrlRestController {

    private static final Logger logger = LoggerFactory.getLogger(ShortUrlRestController.class);

    private final ShortUrlService shortUrlService;
    private final StatisticService statisticService;
    private final SafeBrowsingService safeBrowsingService;

    public ShortUrlRestController(ShortUrlService shortUrlService, StatisticService statisticService, SafeBrowsingService safeBrowsingService) {
        this.shortUrlService = shortUrlService;
        this.statisticService = statisticService;
        this.safeBrowsingService = safeBrowsingService;
    }

    @Value("${app.baseUrl}")
    private String baseUrl;

    @PostMapping("/create_new_short_url")
    public ResponseEntity<ReturnCreatedShortUrlDTO> createNewShortUrl(@RequestBody CreateShortUrlRequestDTO shortUrlRequest, HttpServletRequest request) {
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
    public ResponseEntity<String> getOriginalUrl(@RequestBody GetOriginalUrlDTO codeRequest) {
        try {
            String code = codeRequest.getCode();
            String originalUrl = shortUrlService.getOriginalUrl(code);
            if (originalUrl == null || originalUrl.equals("na")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("此短網址尚未建立! Original url not found!");
            } else if (originalUrl.equals("ban")) {
                return ResponseEntity.status(HttpStatus.GONE).body("此短網址已失效! The short url is banned.");
            } else {
                return ResponseEntity.ok(originalUrl);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("內部伺服器錯誤! Internal server error!");
        }
    }

}
