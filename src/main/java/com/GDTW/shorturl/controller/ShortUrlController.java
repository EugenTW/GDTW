package com.GDTW.shorturl.controller;

import com.GDTW.service.StatisticService;
import com.GDTW.shorturl.model.ShortUrlService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.NoHandlerFoundException;

import com.GDTW.service.IdEncoderDecoderService;

import java.io.IOException;

@Controller
public class ShortUrlController {

    private final ShortUrlService shortUrlService;
    private final StatisticService statisticService;

    public ShortUrlController(ShortUrlService shortUrlService, StatisticService statisticService) {
        this.shortUrlService = shortUrlService;
        this.statisticService = statisticService;
    }

    @GetMapping("/short_url")
    public String shortUrl() {
        return "forward:/short_url.html";
    }

    @GetMapping("/short_url_redirection")
    public String shortUrlRedirection() {
        return "forward:/short_url_redirection.html";
    }

    @GetMapping("/404_short_url")
    public String shortUrl404Redirection() {
        return "forward:/404_short_url.html";
    }

    @GetMapping("/{code:[a-zA-Z0-9]{4}}")
    public void redirectToPage(@PathVariable String code, HttpServletResponse response) throws IOException {
        String originalUrl = shortUrlService.getOriginalUrl(code);
        statisticService.incrementShortUrlUsed();
        if (originalUrl == null || originalUrl.equals("na")) {
            response.sendRedirect("/404_short_url");
            return;
        }
        response.sendRedirect("/short_url_redirection?code=" + code);
    }

    @GetMapping("/s/{code:[a-zA-Z0-9]{4}}")
    public void oldRedirectToPage(@PathVariable String code, HttpServletResponse response) throws IOException {
        String originalUrl = shortUrlService.getOriginalUrl(code);
        statisticService.incrementShortUrlUsed();
        if (originalUrl == null || originalUrl.equals("na")) {
            response.sendRedirect("/404_short_url");
            return;
        }
        response.sendRedirect("/short_url_redirection?code=" + code);
    }
}

