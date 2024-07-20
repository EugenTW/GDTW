package com.GDTW.shorturl.controller;

import com.GDTW.shorturl.model.CreateShortUrlRequestDTO;
import com.GDTW.shorturl.model.GetOriginalUrlDTO;
import com.GDTW.shorturl.model.ShortUrlService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/su_api")
public class ShortUrlRestController {

    private final ShortUrlService shortUrlService;

    @Autowired
    public ShortUrlRestController(ShortUrlService shortUrlService) {
        this.shortUrlService = shortUrlService;
    }

    @PostMapping("/create_new_short_url")
    public ResponseEntity<String> createNewShortUrl(@RequestBody CreateShortUrlRequestDTO shortUrlRequest, HttpServletRequest request) {
        try {
            String currentUrl = request.getRequestURL().toString();
            String baseUrl = currentUrl.substring(0, currentUrl.indexOf(request.getRequestURI()));
            String originalUrl = shortUrlRequest.getOriginalUrl();
            String originalIp = request.getRemoteAddr();

            System.out.println("Original URL: " + originalUrl);
            System.out.println("Original IP: " + originalIp);

            String shortUrl = shortUrlService.createNewShortUrl(originalUrl, originalIp);

            if (shortUrl != null) {
                String fullShortUrl = baseUrl + "/s/" + shortUrl;
                return ResponseEntity.ok(fullShortUrl);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("短網址建立失敗!請稍後再次嘗試!");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("內部伺服器錯誤!請等待站方維修!");
        }
    }

    @PostMapping("/get_original_url")
    public ResponseEntity<String> getOriginalUrl(@RequestBody GetOriginalUrlDTO codeRequest) {
        try {
            String code = codeRequest.getCode();
            String originalUrl = shortUrlService.getOriginalUrl(code);
            if (originalUrl == null || originalUrl.equals("na")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("此短網址尚未建立!請重新確認!");
            } else if (originalUrl.equals("ban")) {
                return ResponseEntity.status(HttpStatus.GONE).body("此短網址已失效!");
            } else {
                return ResponseEntity.ok(originalUrl);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("內部伺服器錯誤!請等待站方維修!");
        }
    }

}
