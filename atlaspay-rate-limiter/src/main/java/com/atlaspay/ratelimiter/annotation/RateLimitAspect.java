package com.atlaspay.ratelimiter.annotation;

import com.atlaspay.ratelimiter.core.RateLimitRuleProvider;
import com.atlaspay.ratelimiter.core.EvaluateRateLimitUseCase;
import com.atlaspay.ratelimiter.core.RateLimitKeyType;
import com.atlaspay.ratelimiter.core.RateLimitRule;
import com.atlaspay.ratelimiter.annotation.RateLimit;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.security.Principal;

@Aspect
@Component
public class RateLimitAspect {

    private final EvaluateRateLimitUseCase evaluateRateLimitUseCase;
    private final RateLimitRuleProvider ruleProvider;

    public RateLimitAspect(EvaluateRateLimitUseCase evaluateRateLimitUseCase, RateLimitRuleProvider ruleProvider) {
        this.evaluateRateLimitUseCase = evaluateRateLimitUseCase;
        this.ruleProvider = ruleProvider;
    }

    @Before("@annotation(rateLimit)")
    public void checkRateLimit(JoinPoint joinPoint, RateLimit rateLimit) {
        RateLimitRule rule = ruleProvider.getRule(rateLimit.ruleId());
        String key = resolveKey(rateLimit.keyType(), rateLimit.ruleId());
        
        evaluateRateLimitUseCase.execute(key, rule);
    }

    private String resolveKey(RateLimitKeyType keyType, String ruleId) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        
        return switch (keyType) {
            case IP -> getClientIp(request) + ":" + ruleId;
            case MERCHANT_ID -> getPrincipalName(request) + ":" + ruleId;
            case API_KEY -> getApiKey(request) + ":" + ruleId;
            case GLOBAL -> "GLOBAL:" + ruleId;
        };
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty()) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
    
    private String getPrincipalName(HttpServletRequest request) {
        Principal principal = request.getUserPrincipal();
        return principal != null ? principal.getName() : "anonymous";
    }
    
    private String getApiKey(HttpServletRequest request) {
        String apiKey = request.getHeader("X-API-Key");
        return apiKey != null ? apiKey : "no-api-key";
    }
}
