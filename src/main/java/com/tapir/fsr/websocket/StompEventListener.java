package com.tapir.fsr.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
public class StompEventListener {
    private static final Logger logger = LoggerFactory.getLogger(StompEventListener.class);
    private final WebSocketSessionRegistry registry;

    public StompEventListener(WebSocketSessionRegistry registry) {
        this.registry = registry;
    }

    @EventListener
    public void handleSessionConnected(SessionConnectedEvent event) {
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = sha.getSessionId();
        registry.register(sessionId);
        logger.debug("STOMP connected: {}", sessionId);
    }

    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = sha.getSessionId();
        registry.unregister(sessionId);
        logger.debug("STOMP disconnected: {}", sessionId);
    }
}
