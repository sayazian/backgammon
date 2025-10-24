package com.coderscampus.backgammon.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Clients subscribe to destinations like /topic/invites or /user/queue/notifications
        config.enableSimpleBroker("/topic", "/queue");
        // Messages sent with convertAndSendToUser will be prefixed with /user
        config.setUserDestinationPrefix("/user");
        // Client sends to /app/** and it gets routed to @MessageMapping handlers
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")                 // Browser connects to /ws
                .setAllowedOriginPatterns("*");     // Tweak for prod whitelisting
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();                      // Optional: SockJS fallback
    }
}
