package com.runmvp.websocket.handler;

import com.runmvp.websocket.registry.WebSocketSessionRegistry;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class RunMvpWebSocketHandler extends TextWebSocketHandler {

    private final WebSocketSessionRegistry registry;

    public RunMvpWebSocketHandler(WebSocketSessionRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId != null) registry.register(userId, session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId != null) registry.deregister(userId);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        // Client pong frames — no action needed
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId != null) registry.deregister(userId);
    }
}
