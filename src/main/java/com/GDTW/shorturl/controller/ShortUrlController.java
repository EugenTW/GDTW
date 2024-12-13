package com.GDTW.shorturl.controller;

import com.GDTW.dailystatistic.model.DailyStatisticService;
import com.GDTW.shorturl.model.ShortUrlService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.IOException;

@Controller
public class ShortUrlController {

    private final ShortUrlService shortUrlService;
    private final DailyStatisticService statisticService;

    public ShortUrlController(ShortUrlService shortUrlService, DailyStatisticService statisticService) {
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

    @GetMapping("/{code:[a-zA-Z0-9]{4}}")
    public String redirectToPage(@PathVariable String code, HttpServletResponse response) throws IOException {
        if (!shortUrlService.checkCodeValid(code)) {
            response.sendRedirect("/error_404");
            return null;
        }
        statisticService.incrementShortUrlUsed();
        return "forward:/short_url_redirection.html";
    }

}

