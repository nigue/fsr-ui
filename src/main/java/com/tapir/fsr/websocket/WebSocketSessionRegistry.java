package com.tapir.fsr.websocket;

import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class WebSocketSessionRegistry {
    private final Set<String> sessions = ConcurrentHashMap.newKeySet();

    public void register(String sessionId) {
        if (sessionId != null) sessions.add(sessionId);
    }

    public void unregister(String sessionId) {
        if (sessionId != null) sessions.remove(sessionId);
    }

    public Set<String> getSessionIds() {
        return Collections.unmodifiableSet(sessions);
    }

    public int count() {
        return sessions.size();
    }
}
