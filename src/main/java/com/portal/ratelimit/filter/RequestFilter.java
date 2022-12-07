package com.portal.ratelimit.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.portal.ratelimit.service.RateLimiter;
import io.github.bucket4j.Bucket;
import io.micrometer.core.instrument.util.StringUtils;

@Component
public class RequestFilter extends OncePerRequestFilter {

	@Autowired
	RateLimiter rateLimiter;
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		
		if(request.getRequestURI().startsWith("/v1")) {
			String tenantId = request.getHeader("X-Tenant");
			if(StringUtils.isNotBlank(tenantId)) {
				Bucket bucket = rateLimiter.resolveBucket(tenantId);
				if(bucket.tryConsume(1)) {
					filterChain.doFilter(request, response);
				} else {
					sendErrorReponse(response, HttpStatus.TOO_MANY_REQUESTS.value());
				}
			} else {
				sendErrorReponse(response, HttpStatus.FORBIDDEN.value());
			}
		} else {
			filterChain.doFilter(request, response);
		}

	}

	private void sendErrorReponse(HttpServletResponse response, int value) {
		HttpServletResponse resp = (HttpServletResponse)response;
		resp.setStatus(value);
		
		resp.setContentType(MediaType.APPLICATION_JSON_VALUE);
	}

}
