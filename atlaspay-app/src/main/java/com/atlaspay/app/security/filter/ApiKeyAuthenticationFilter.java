package com.atlaspay.app.security.filter;

import com.atlaspay.app.security.authentication.AtlasPayAuthenticationToken;
import com.atlaspay.identity.domain.model.ApiKey;
import com.atlaspay.identity.domain.repository.ApiKeyRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Component
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private final ApiKeyRepository apiKeyRepository;

    public ApiKeyAuthenticationFilter(ApiKeyRepository apiKeyRepository) {
        this.apiKeyRepository = apiKeyRepository;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        if (token.startsWith("pk_") || token.startsWith("sk_")) {
            String hashToLookup = token;
            if (token.startsWith("sk_")) {
                hashToLookup = com.atlaspay.shared.util.HashingUtils.sha256Hex(token);
            }

            Optional<ApiKey> apiKeyOpt = apiKeyRepository.findByKeyHash(hashToLookup);

            if (apiKeyOpt.isPresent()) {
                ApiKey apiKey = apiKeyOpt.get();
                if (apiKey.isActive() && SecurityContextHolder.getContext().getAuthentication() == null) {
                    AtlasPayAuthenticationToken authToken = new AtlasPayAuthenticationToken(
                            String.valueOf(apiKey.getMerchantId()),
                            token,
                            AtlasPayAuthenticationToken.AuthType.API_KEY
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
