package com.gdtw.shorturl.controller;

import com.gdtw.shorturl.model.ShortUrlService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.IOException;

@Controller
public class ShortUrlController {

    private final ShortUrlService shortUrlService;

    public ShortUrlController(ShortUrlService shortUrlService) {
        this.shortUrlService = shortUrlService;
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
        return "forward:/short_url_redirection.html";
    }

}

