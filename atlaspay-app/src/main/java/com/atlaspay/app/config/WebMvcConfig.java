package com.atlaspay.app.config;

import com.atlaspay.ratelimiter.web.GlobalRateLimitInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final GlobalRateLimitInterceptor globalRateLimitInterceptor;

    public WebMvcConfig(GlobalRateLimitInterceptor globalRateLimitInterceptor) {
        this.globalRateLimitInterceptor = globalRateLimitInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Apply global rate limiting to all API endpoints
        registry.addInterceptor(globalRateLimitInterceptor)
                .addPathPatterns("/api/**");
    }
}
