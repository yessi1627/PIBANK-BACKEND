package com.pibank.backend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pibank.backend.dto.response.ApiResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Slf4j
public class RateLimitingFilter extends OncePerRequestFilter {

    @Value("${pibank.security.rate-limit.requests-per-minute:60}")
    private int requestsPerMinute;

    @Value("${pibank.security.rate-limit.requests-per-minute-auth:10}")
    private int authRequestsPerMinute;

    private final Map<String, RequestCounter> requestCounts = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain chain
    ) throws ServletException, IOException {

        String clientIp = getClientIp(request);
        boolean isAuthEndpoint = request.getServletPath().startsWith("/auth/");
        int limit = isAuthEndpoint ? authRequestsPerMinute : requestsPerMinute;

        String key = clientIp + ":" + (isAuthEndpoint ? "auth" : "general");
        RequestCounter counter = requestCounts.computeIfAbsent(key, k -> new RequestCounter());

        if (counter.isLimitExceeded(limit)) {
            log.warn("Rate limit superado para IP: {} en endpoint: {}", clientIp, request.getServletPath());
            response.setStatus(429);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setHeader("Retry-After", "60");
            objectMapper.writeValue(
                response.getWriter(),
                ApiResponse.error("Demasiadas solicitudes. Por favor espere un momento.", "RATE_LIMIT_EXCEEDED")
            );
            return;
        }

        chain.doFilter(request, response);
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private static class RequestCounter {
        private final AtomicInteger count = new AtomicInteger(0);
        private long windowStart = System.currentTimeMillis();

        synchronized boolean isLimitExceeded(int limit) {
            long now = System.currentTimeMillis();
            if (now - windowStart > 60_000) {
                count.set(0);
                windowStart = now;
            }
            return count.incrementAndGet() > limit;
        }
    }
}
