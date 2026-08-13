package com.atlaspay.app.security.websocket;

import com.atlaspay.app.security.authentication.AtlasPayAuthenticationToken;
import com.atlaspay.identity.infrastructure.adapter.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtChannelInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String jwt = authHeader.substring(7);
                String merchantId = jwtService.extractMerchantId(jwt);
                
                if (merchantId != null && jwtService.isTokenValid(jwt)) {
                    AtlasPayAuthenticationToken authToken = new AtlasPayAuthenticationToken(
                            merchantId,
                            jwt,
                            AtlasPayAuthenticationToken.AuthType.JWT
                    );
                    accessor.setUser(authToken);
                }
            }
        }
        return message;
    }
}
