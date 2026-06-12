package com.runmvp.auth.adapter.in.web;

import com.runmvp.BaseIntegrationTest;
import com.runmvp.auth.application.port.out.GoogleTokenVerifier;
import com.runmvp.shared.exception.BusinessException;
import com.runmvp.shared.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class AuthControllerIT extends BaseIntegrationTest {

    @Autowired private TestRestTemplate restTemplate;
    @MockitoBean private GoogleTokenVerifier googleTokenVerifier;

    @Test
    void postAuthGoogle_newUser_returns200WithTokens() {
        when(googleTokenVerifier.verify("valid-id-token")).thenReturn(
            new GoogleTokenVerifier.GoogleIdTokenPayload("sub-new-it", "new@it.com", "IT User", null)
        );

        ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
            "/auth/google",
            Map.of("idToken", "valid-id-token"),
            AuthResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().accessToken()).isNotBlank();
        assertThat(response.getBody().refreshToken()).isNotBlank();
        assertThat(response.getBody().isNewUser()).isTrue();
    }

    @Test
    void postAuthGoogle_invalidToken_returns401() {
        when(googleTokenVerifier.verify("bad-token"))
            .thenThrow(new BusinessException(ErrorCode.GOOGLE_TOKEN_INVALID));

        ResponseEntity<String> response = restTemplate.postForEntity(
            "/auth/google",
            Map.of("idToken", "bad-token"),
            String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).contains("GOOGLE_TOKEN_INVALID");
    }

    @Test
    void postAuthRefresh_validToken_returns200() {
        when(googleTokenVerifier.verify("token-for-refresh")).thenReturn(
            new GoogleTokenVerifier.GoogleIdTokenPayload("sub-refresh", "r@it.com", "Refresh User", null)
        );
        ResponseEntity<AuthResponse> auth = restTemplate.postForEntity(
            "/auth/google",
            Map.of("idToken", "token-for-refresh"),
            AuthResponse.class
        );
        String refreshToken = auth.getBody().refreshToken();

        ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
            "/auth/refresh",
            Map.of("refreshToken", refreshToken),
            AuthResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().accessToken()).isNotBlank();
        assertThat(response.getBody().refreshToken()).isNotBlank();
        assertThat(response.getBody().refreshToken()).isNotEqualTo(refreshToken);
    }

    @Test
    void postAuthRefresh_unknownToken_returns401() {
        ResponseEntity<String> response = restTemplate.postForEntity(
            "/auth/refresh",
            Map.of("refreshToken", "unknown"),
            String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void postAuthLogout_validToken_returns204() {
        when(googleTokenVerifier.verify("token-for-logout")).thenReturn(
            new GoogleTokenVerifier.GoogleIdTokenPayload("sub-logout", "l@it.com", "Logout User", null)
        );
        ResponseEntity<AuthResponse> auth = restTemplate.postForEntity(
            "/auth/google",
            Map.of("idToken", "token-for-logout"),
            AuthResponse.class
        );

        ResponseEntity<Void> response = restTemplate.postForEntity(
            "/auth/logout",
            Map.of("refreshToken", auth.getBody().refreshToken()),
            Void.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }
}
