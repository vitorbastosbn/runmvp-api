package com.runmvp.websocket.config;

import com.runmvp.websocket.auth.WebSocketHandshakeInterceptor;
import com.runmvp.websocket.handler.RunMvpWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final RunMvpWebSocketHandler handler;
    private final WebSocketHandshakeInterceptor interceptor;

    public WebSocketConfig(RunMvpWebSocketHandler handler,
                           WebSocketHandshakeInterceptor interceptor) {
        this.handler = handler;
        this.interceptor = interceptor;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(handler, "/ws")
            .addInterceptors(interceptor)
            .setAllowedOriginPatterns("*");
    }
}
