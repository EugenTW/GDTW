package com.gdtw.cssjsminify.controller;

import com.gdtw.cssjsminify.service.CssJsMinifyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/cj_api")
public class CssJsMinifyRestController {

    private final CssJsMinifyService minifyService;

    public CssJsMinifyRestController(CssJsMinifyService minifyService) {
        this.minifyService = minifyService;
    }

    @PostMapping("/css_js_minifier")
    public ResponseEntity<Map<String, String>> minifyCode(@RequestBody Map<String, String> requestBody) {
        String source = requestBody.getOrDefault("source", "").trim();

        if (source.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("type", "ERROR", "error", "Empty input."));
        }

        Map<String, String> result = minifyService.autoDetectAndMinify(source);
        return ResponseEntity.ok(result);
    }
}