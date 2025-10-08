package com.gdtw.general.filter.bot;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

public class BotDetectionFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger("botLogger");

    private final BotFilterProperties config;

    private static final List<String> ALLOWED_BOTS = List.of(
            "googlebot", "bingbot", "slurp", "duckduckbot", "baiduspider",
            "yandexbot", "facebot", "twitterbot"
    );

    private static final List<String> SUSPICIOUS_AGENTS = List.of(
            "curl", "wget", "python", "scrapy", "httpclient", "bot", "crawler", "libwww"
    );

    private static final List<String> STATIC_EXTENSIONS = List.of(
            ".html", ".css", ".js", ".png", ".jpg", ".webp", ".svg", ".ico"
    );

    public BotDetectionFilter(BotFilterProperties config) {
        this.config = config;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (!(request instanceof HttpServletRequest req && response instanceof HttpServletResponse)) {
            chain.doFilter(request, response);
            return;
        }

        String path = req.getRequestURI();
        String ua = Optional.ofNullable(req.getHeader("User-Agent")).orElse("").toLowerCase();
        String ip = Optional.ofNullable(req.getRemoteAddr()).orElse("unknown");

        if ("127.0.0.1".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip)) {
            chain.doFilter(request, response);
            return;
        }

        for (String bot : ALLOWED_BOTS) {
            if (ua.contains(bot)) {
                logger.debug("Allowed bot detected: UA='{}' IP='{}' Path='{}'", ua, ip, path);
                chain.doFilter(request, response);
                return;
            }
        }

        for (String ext : STATIC_EXTENSIONS) {
            if (path.endsWith(ext)) {
                chain.doFilter(request, response);
                return;
            }
        }

        boolean matched = config.getProtectedPathPrefixes().stream()
                .anyMatch(path::startsWith);

        if (!matched) {
            chain.doFilter(request, response);
            return;
        }

        if (ua.isEmpty() || SUSPICIOUS_AGENTS.stream().anyMatch(ua::contains)) {
            logger.warn("Suspicious UA detected: UA='{}' IP='{}' Path='{}'", ua, ip, path);
        }

        chain.doFilter(request, response);
    }

}
