package com.runmvp.websocket.auth;

import com.runmvp.auth.application.port.out.JwtService;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Component
public class WebSocketHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtService jwtService;

    public WebSocketHandshakeInterceptor(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        String auth = request.getHeaders().getFirst("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) return false;

        String token = auth.substring(7);
        if (!jwtService.isValid(token)) return false;

        attributes.put("userId", jwtService.extractUserId(token));
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception ex) {}
}
