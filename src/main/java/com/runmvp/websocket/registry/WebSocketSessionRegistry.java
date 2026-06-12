package com.runmvp.websocket.registry;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebSocketSessionRegistry {

    private final Map<Long, WebSocketSession> sessions = new ConcurrentHashMap<>();

    public void register(Long userId, WebSocketSession session) {
        sessions.put(userId, session);
    }

    public void deregister(Long userId) {
        sessions.remove(userId);
    }

    public Optional<WebSocketSession> get(Long userId) {
        return Optional.ofNullable(sessions.get(userId));
    }

    public Map<Long, WebSocketSession> all() {
        return Map.copyOf(sessions);
    }
}
