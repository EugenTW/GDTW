package com.GDTW.imgshare.controller;

import com.GDTW.dailystatistic.model.DailyStatisticService;
import com.GDTW.imgshare.model.AlbumCreationRequestDTO;
import com.GDTW.imgshare.model.ImgShareService;
import com.google.common.util.concurrent.RateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/is_api")
public class ImgShareRestController {
    private final RateLimiter albumCreationRateLimiter = RateLimiter.create(20.0); // 20 requests per second
    private final RateLimiter albumPasswordRateLimiter = RateLimiter.create(50.0); // 50 requests per second
    private final RateLimiter imagePasswordRateLimiter = RateLimiter.create(50.0); // 50 requests per second
    private static final Logger logger = LoggerFactory.getLogger(ImgShareRestController.class);
    private final ImgShareService imgShareService;
    private final DailyStatisticService dailyStatisticService;

    public ImgShareRestController(ImgShareService imgShareService, DailyStatisticService dailyStatisticService) {
        this.imgShareService = imgShareService;
        this.dailyStatisticService = dailyStatisticService;
    }

    @Value("${app.baseUrl}")
    private String baseUrl;

    @PostMapping("/create_new_album")
    public ResponseEntity<Map<String, String>> createNewAlbum(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam("expiryDays") int expiryDays,
            @RequestParam("nsfw") boolean nsfw,
            @RequestParam(value = "password", required = false) String password,
            HttpServletRequest request) {

        if (!albumCreationRateLimiter.tryAcquire()) {
            Map<String, String> response = new HashMap<>();
            logger.warn("Album creation limit exceeded.");
            response.put("error", "Too many requests. Please try again later.");
            return ResponseEntity.status(429).body(response);
        }

        // Get client IP address
        String originalIp = getClientIp(request);
        AlbumCreationRequestDTO requestDTO = new AlbumCreationRequestDTO(files, expiryDays, nsfw, password, originalIp);
        Map<String, String> response = imgShareService.createNewAlbumAndImage(requestDTO);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/isAlbumPasswordNeeded")
    public ResponseEntity<Map<String, Object>> isAlbumPasswordNeeded(@RequestBody Map<String, String> request) {
        String code = request.get("code");
        Map<String, Object> result = imgShareService.isShareImageAlbumPasswordProtected(code);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/isImagePasswordNeeded")
    public ResponseEntity<Map<String, Object>> isImagePasswordNeeded(@RequestBody Map<String, String> request) {
        String code = request.get("code");
        Map<String, Object> result = imgShareService.isShareImagePasswordProtected(code);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/checkAlbumPassword")
    public ResponseEntity<Map<String, Object>> checkAlbumPassword(@RequestBody Map<String, String> request) {
        // Rate limiting: If too many requests, return 429 error
        if (!albumPasswordRateLimiter.tryAcquire()) {
            logger.warn("Album password checking limit exceeded.");
            return createTooManyRequestsResponse();
        }

        // Extract request parameters
        String code = request.get("code");
        String password = request.get("password");

        // Check the album password
        Map<String, Object> result = imgShareService.checkAlbumPassword(code, password);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/checkImagePassword")
    public ResponseEntity<Map<String, Object>> checkImagePassword(@RequestBody Map<String, String> request) {
        // Rate limiting: If too many requests, return 429 error
        if (!imagePasswordRateLimiter.tryAcquire()) {
            logger.warn("Image password checking limit exceeded.");
            return createTooManyRequestsResponse();
        }

        // Extract request parameters
        String code = request.get("code");
        String password = request.get("password");

        // Check the image password
        Map<String, Object> result = imgShareService.checkImagePassword(code, password);
        return ResponseEntity.ok(result);
    }


    @PostMapping("/downloadAlbumImages")
    public ResponseEntity<Map<String, Object>> downloadAlbumImages(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        Map<String, Object> result = imgShareService.getAlbumImages(token);
        if (result.containsKey("error")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
        }
        dailyStatisticService.incrementImgAlbumUsed();
        return ResponseEntity.ok(result);
    }

    @PostMapping("/downloadSingleImage")
    public ResponseEntity<Map<String, Object>> downloadSingleImage(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        Map<String, Object> result = imgShareService.getSingleImage(token);
        if (result.containsKey("error")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
        }
        dailyStatisticService.incrementImgUsed();
        return ResponseEntity.ok(result);
    }

    // ===============================================================================================
    // Tools
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isEmpty()) {
            ip = ip.split(",")[0];
        } else {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    private ResponseEntity<Map<String, Object>> createTooManyRequestsResponse() {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Too many requests. Please try again later.");
        return ResponseEntity.status(429).body(response);
    }

}
