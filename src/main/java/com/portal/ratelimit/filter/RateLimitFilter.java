package com.portal.ratelimit.filter;

import com.portal.ratelimit.service.RateLimiter;
import io.github.bucket4j.Bucket;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@AllArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {
    RateLimiter rateLimiter;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String ipAddress = getClientIP(request);
        if (StringUtils.isNotBlank(ipAddress)) {
            Bucket bucket = rateLimiter.resolveBucket(ipAddress);
            if (bucket.tryConsume(1)) {
                filterChain.doFilter(request, response);
            } else {
                sendErrorResponse(response, HttpStatus.TOO_MANY_REQUESTS.value());
            }
        } else {
            sendErrorResponse(response, HttpStatus.FORBIDDEN.value());
        }

    }

    private String getClientIP(HttpServletRequest request) {
        return request.getRemoteAddr();
    }

    private void sendErrorResponse(HttpServletResponse response, int value) {
        response.setStatus(value);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    }
}
