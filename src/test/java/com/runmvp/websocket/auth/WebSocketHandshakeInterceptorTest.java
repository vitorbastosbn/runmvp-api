package com.runmvp.websocket.auth;

import com.runmvp.auth.application.port.out.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.http.server.ServletServerHttpRequest;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebSocketHandshakeInterceptorTest {

    @Mock JwtService jwtService;
    @InjectMocks WebSocketHandshakeInterceptor interceptor;

    @Test
    void beforeHandshake_validToken_returnsTrue() throws Exception {
        when(jwtService.isValid("valid")).thenReturn(true);
        when(jwtService.extractUserId("valid")).thenReturn(42L);

        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("Authorization", "Bearer valid");
        Map<String, Object> attrs = new HashMap<>();

        boolean result = interceptor.beforeHandshake(
            new ServletServerHttpRequest(req), null, null, attrs);

        assertThat(result).isTrue();
        assertThat(attrs.get("userId")).isEqualTo(42L);
    }

    @Test
    void beforeHandshake_noToken_returnsFalse() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        Map<String, Object> attrs = new HashMap<>();

        boolean result = interceptor.beforeHandshake(
            new ServletServerHttpRequest(req), null, null, attrs);

        assertThat(result).isFalse();
    }

    @Test
    void beforeHandshake_invalidToken_returnsFalse() throws Exception {
        when(jwtService.isValid("bad")).thenReturn(false);
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("Authorization", "Bearer bad");
        Map<String, Object> attrs = new HashMap<>();

        boolean result = interceptor.beforeHandshake(
            new ServletServerHttpRequest(req), null, null, attrs);

        assertThat(result).isFalse();
    }
}
