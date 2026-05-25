package com.shelfflow.services.common.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class AccessLogFilter extends OncePerRequestFilter {
    private static final Logger ACCESS_LOGGER = LoggerFactory.getLogger("shelfflow.access");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        long startedAt = System.currentTimeMillis();
        try {
            filterChain.doFilter(request, response);
        } finally {
            long durationMs = System.currentTimeMillis() - startedAt;
            Object requestId = request.getAttribute(RequestContext.REQUEST_ID_ATTRIBUTE);
            ACCESS_LOGGER.info(
                    "requestId={} method={} path={} status={} durationMs={} remoteAddr={} userAgent={}",
                    requestId,
                    request.getMethod(),
                    request.getRequestURI(),
                    response.getStatus(),
                    durationMs,
                    request.getRemoteAddr(),
                    request.getHeader("User-Agent")
            );
        }
    }
}
