package com.gdtw.general.filter.http405;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class MethodNotAllowedLoggingFilter implements Filter {

    private static final Logger suspiciousLogger = LoggerFactory.getLogger("http405Logger");
    private static final String HEADER_X_FORWARDED_FOR = "X-Forwarded-For";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        StatusCaptureResponseWrapper wrappedResp = new StatusCaptureResponseWrapper((HttpServletResponse) response);

        chain.doFilter(request, wrappedResp);

        if (wrappedResp.getStatus() == HttpServletResponse.SC_METHOD_NOT_ALLOWED) {
            String ip = req.getHeader(HEADER_X_FORWARDED_FOR);
            String method = req.getMethod();
            String uri = req.getRequestURI();
            String query = req.getQueryString();
            String userAgent = req.getHeader("User-Agent");

            suspiciousLogger.warn(
                    """
                    
                    === HTTP 405 Detected ===
                    Method    : {}
                    URI       : {}
                    Query     : {}
                    Client IP : {}
                    User-Agent: {}
                    
                    """,
                    method, uri, query == null ? "" : query, ip, userAgent
            );
        }
    }
}

