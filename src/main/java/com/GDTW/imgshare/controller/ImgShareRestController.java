package com.GDTW.imgshare.controller;

import com.GDTW.dailystatistic.model.DailyStatisticService;
import com.GDTW.general.exception.InsufficientDiskSpaceException;
import com.GDTW.general.service.ratelimiter.RateLimiterService;
import com.GDTW.imgshare.model.AlbumCreationRequestDTO;
import com.GDTW.imgshare.model.ImgShareService;
import com.GDTW.imgshare.model.TokenRequestDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(ImgShareRestController.class);

    private final ImgShareService imgShareService;
    private final DailyStatisticService dailyStatisticService;
    private final RateLimiterService rateLimiterService;

    public ImgShareRestController(ImgShareService imgShareService, DailyStatisticService dailyStatisticService, RateLimiterService rateLimiterService) {
        this.imgShareService = imgShareService;
        this.dailyStatisticService = dailyStatisticService;
        this.rateLimiterService = rateLimiterService;
    }

    @PostMapping("/create_new_album")
    public ResponseEntity<Map<String, String>> createNewAlbum(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam("expiryDays") int expiryDays,
            @RequestParam("nsfw") boolean nsfw,
            @RequestParam(value = "password", required = false) String password,
            HttpServletRequest request) {

        String originalIp = request.getHeader("X-Forwarded-For");
        rateLimiterService.checkCreateShareImageLimit(originalIp);

        AlbumCreationRequestDTO requestDTO = new AlbumCreationRequestDTO(files, expiryDays, nsfw, password, originalIp);

        try {
            Map<String, String> response = imgShareService.createNewAlbumAndImage(requestDTO);
            return ResponseEntity.ok(response);
        } catch (InsufficientDiskSpaceException e) {
            logger.error("Failed to create album: {}", e.getMessage());
            Map<String, String> response = new HashMap<>();
            response.put("error", "The server has reached its maximum capacity limit. Image Share service is temporarily suspended.");
            return ResponseEntity.status(507).body(response);
        } catch (Exception e) {
            logger.error("Unexpected error occurred: {}", e.getMessage());
            Map<String, String> response = new HashMap<>();
            response.put("error", "An unexpected error occurred. Please try again later.");
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/isAlbumPasswordNeeded")
    public ResponseEntity<Map<String, Object>> isAlbumPasswordNeeded(@RequestBody Map<String, String> request, HttpServletRequest originRequest) {
        String originalIp = originRequest.getHeader("X-Forwarded-For");
        rateLimiterService.checkGetShareImageLimit(originalIp);
        String code = request.get("code");
        Map<String, Object> result = imgShareService.isShareImageAlbumPasswordProtected(code);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/isImagePasswordNeeded")
    public ResponseEntity<Map<String, Object>> isImagePasswordNeeded(@RequestBody Map<String, String> request, HttpServletRequest originRequest) {
        String originalIp = originRequest.getHeader("X-Forwarded-For");
        rateLimiterService.checkGetShareImageLimit(originalIp);
        String code = request.get("code");
        Map<String, Object> result = imgShareService.isShareImagePasswordProtected(code);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/checkAlbumPassword")
    public ResponseEntity<Map<String, Object>> checkAlbumPassword(@RequestBody Map<String, String> request, HttpServletRequest originRequest) {

        String originalIp = originRequest.getHeader("X-Forwarded-For");
        rateLimiterService.checkGetShareImageLimit(originalIp);

        // Extract request parameters
        String code = request.get("code");
        String password = request.get("password");

        // Check the album password
        Map<String, Object> result = imgShareService.checkAlbumPassword(code, password);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/checkImagePassword")
    public ResponseEntity<Map<String, Object>> checkImagePassword(@RequestBody Map<String, String> request, HttpServletRequest originRequest) {

        String originalIp = originRequest.getHeader("X-Forwarded-For");
        rateLimiterService.checkGetShareImageLimit(originalIp);

        // Extract request parameters
        String code = request.get("code");
        String password = request.get("password");

        // Check the image password
        Map<String, Object> result = imgShareService.checkImagePassword(code, password);
        return ResponseEntity.ok(result);
    }


    @PostMapping("/downloadAlbumImages")
    public ResponseEntity<Map<String, Object>> downloadAlbumImages(@RequestBody TokenRequestDTO request, HttpServletRequest originRequest) {

        String token = request.getToken();
        String originalIp = originRequest.getHeader("X-Forwarded-For");
        rateLimiterService.checkGetShareImageLimit(originalIp);

        Map<String, Object> result = imgShareService.getAlbumImages(token);
        if (result.containsKey("error")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
        }
        dailyStatisticService.incrementImgAlbumUsed();
        return ResponseEntity.ok(result);
    }

    @PostMapping("/downloadSingleImage")
    public ResponseEntity<Map<String, Object>> downloadSingleImage(@RequestBody TokenRequestDTO request, HttpServletRequest originRequest) {

        String token = request.getToken();
        String originalIp = originRequest.getHeader("X-Forwarded-For");
        rateLimiterService.checkGetShareImageLimit(originalIp);

        Map<String, Object> result = imgShareService.getSingleImage(token);
        if (result.containsKey("error")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
        }
        dailyStatisticService.incrementImgUsed();
        return ResponseEntity.ok(result);
    }

}
