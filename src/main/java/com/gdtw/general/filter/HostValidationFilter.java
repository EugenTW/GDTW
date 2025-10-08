package com.gdtw.general.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.Filter;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class HostValidationFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        String host = ((HttpServletRequest) request).getHeader("Host");
        if (host == null ||
                !(host.equalsIgnoreCase("gdtw.org") ||
                        host.startsWith("localhost") ||
                        host.startsWith("127.0.0.1"))) {
            ((HttpServletResponse) response).sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid Host");
            return;
        }
        chain.doFilter(request, response);
    }

}

