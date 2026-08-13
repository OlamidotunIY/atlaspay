package com.atlaspay.ratelimiter.web;

import com.atlaspay.ratelimiter.core.RateLimitRuleProvider;
import com.atlaspay.ratelimiter.core.EvaluateRateLimitUseCase;
import com.atlaspay.ratelimiter.core.RateLimitRule;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class GlobalRateLimitInterceptor implements HandlerInterceptor {

    private final EvaluateRateLimitUseCase evaluateRateLimitUseCase;
    private final RateLimitRuleProvider ruleProvider;

    public GlobalRateLimitInterceptor(EvaluateRateLimitUseCase evaluateRateLimitUseCase, RateLimitRuleProvider ruleProvider) {
        this.evaluateRateLimitUseCase = evaluateRateLimitUseCase;
        this.ruleProvider = ruleProvider;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // We use a global rule synced to Redis, e.g., "global_ip_tb"
        RateLimitRule rule = ruleProvider.getRule("global_ip_tb");
        
        String ip = getClientIp(request);
        String key = ip + ":global_ip_tb";
        
        // This will throw RateLimitExceededException if exceeded, which is handled by global exception handler
        var result = evaluateRateLimitUseCase.execute(key, rule);
        
        response.setHeader("X-RateLimit-Limit", String.valueOf(result.limit()));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(result.remainingRequests()));
        
        return true;
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty()) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}
