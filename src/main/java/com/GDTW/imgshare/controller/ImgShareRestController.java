package com.GDTW.imgshare.controller;

import com.GDTW.dailystatistic.model.DailyStatisticService;
import com.GDTW.imgshare.model.AlbumCreationRequestDTO;
import com.GDTW.imgshare.model.ImgShareService;
import com.GDTW.shorturl.model.CreateShortUrlRequestDTO;
import com.GDTW.shorturl.model.ReturnCreatedShortUrlDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
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

    public ImgShareRestController(ImgShareService imgShareService, DailyStatisticService dailyStatisticService) {
        this.imgShareService = imgShareService;
        this.dailyStatisticService = dailyStatisticService;
    }

    @Value("${app.baseUrl}")
    private String baseUrl;

    @PostMapping("/create_new_album")
    public  ResponseEntity<Map<String, String>> createNewAlbum(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam("expiryDays") int expiryDays,
            @RequestParam("nsfw") boolean nsfw,
            @RequestParam(value = "password", required = false) String password,
            HttpServletRequest request) {

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




// ===============================================================================================
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

}
