package com.gdtw.cssjsminify.controller;

import com.gdtw.cssjsminify.service.CssJsMinifyService;
import com.gdtw.dailystatistic.model.DailyStatisticService;
import com.gdtw.general.helper.ratelimiter.RateLimiterHelper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/cj_api")
public class CssJsMinifyRestController {

    private final CssJsMinifyService minifyService;
    private final RateLimiterHelper rateLimiterService;
    private final DailyStatisticService dailyStatisticService;

    public CssJsMinifyRestController(CssJsMinifyService minifyService, RateLimiterHelper rateLimiterService, DailyStatisticService dailyStatisticService) {
        this.minifyService = minifyService;
        this.rateLimiterService = rateLimiterService;
        this.dailyStatisticService = dailyStatisticService;
    }

    @PostMapping("/css_js_minifier")
    public ResponseEntity<Map<String, String>> minifyCode(@RequestBody Map<String, String> requestBody, HttpServletRequest originRequest) {

        String originalIp = originRequest.getHeader("X-Forwarded-For");
        rateLimiterService.checkCssJsMinifyLimit(originalIp);
        dailyStatisticService.incrementCssJsMinified();

        String source = requestBody.getOrDefault("source", "").trim();

        if (source.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("type", "ERROR", "error", "Empty input."));
        }

        final int MAX_INPUT_SIZE = 250 * 1024;
        if (source.length() > MAX_INPUT_SIZE) {
            return ResponseEntity.badRequest().body(
                    Map.of("type", "ERROR",
                            "error", "Input too large. Please split and compress separately."));
        }

        Map<String, String> result = minifyService.autoDetectAndMinify(source);
        return ResponseEntity.ok(result);
    }

}