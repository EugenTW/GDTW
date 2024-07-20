package com.GDTW.shorturl.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.IOException;

@Controller
public class ShortUrlController {

    @GetMapping("/short_url")
    public String shortUrl() {
        return "redirect:/short_url.html";
    }

    @GetMapping("/short_url_redirection")
    public String shortUrlRedirection() {
        return "redirect:/short_url_redirection.html";
    }

    @GetMapping("/s/{code}")
    public void redirectToPage(@PathVariable String code, HttpServletResponse response) throws IOException {
        if (code.matches("\\w{4}")) {
            response.sendRedirect("/short_url_redirection.html?code=" + code);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
}

