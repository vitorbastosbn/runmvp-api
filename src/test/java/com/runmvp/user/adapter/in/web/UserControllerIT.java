package com.runmvp.user.adapter.in.web;

import com.runmvp.BaseIntegrationTest;
import com.runmvp.auth.adapter.in.web.AuthResponse;
import com.runmvp.auth.application.port.out.GoogleTokenVerifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserControllerIT extends BaseIntegrationTest {

    @Autowired private TestRestTemplate restTemplate;
    @MockitoBean private GoogleTokenVerifier googleTokenVerifier;

    private String accessToken;
    private String userEmail;

    @BeforeEach
    void login() {
        String unique = UUID.randomUUID().toString().substring(0, 8);
        userEmail = "user-" + unique + "@ctrl.com";
        when(googleTokenVerifier.verify("test-id-token")).thenReturn(
            new GoogleTokenVerifier.GoogleIdTokenPayload(
                "sub-user-ctrl-" + unique, userEmail, "Ctrl User", null)
        );
        ResponseEntity<AuthResponse> auth = restTemplate.postForEntity(
            "/auth/google", Map.of("idToken", "test-id-token"), AuthResponse.class
        );
        accessToken = auth.getBody().accessToken();
    }

    private HttpHeaders bearerHeaders() {
        HttpHeaders h = new HttpHeaders();
        h.setBearerAuth(accessToken);
        return h;
    }

    @Test
    void getMe_returns200WithUserData() {
        ResponseEntity<UserResponse> resp = restTemplate.exchange(
            "/me", HttpMethod.GET,
            new HttpEntity<>(bearerHeaders()), UserResponse.class
        );

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().email()).isEqualTo(userEmail);
        assertThat(resp.getBody().entitlement()).isEqualTo("FREE");
    }

    @Test
    void getMe_noToken_returns401() {
        ResponseEntity<String> resp = restTemplate.getForEntity("/me", String.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void patchMe_updatesName_returns200() {
        ResponseEntity<UserResponse> resp = restTemplate.exchange(
            "/me", HttpMethod.PATCH,
            new HttpEntity<>(Map.of("name", "New Name", "avatarUrl", "https://new.img/a.jpg"),
                bearerHeaders()),
            UserResponse.class
        );

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().name()).isEqualTo("New Name");
    }

    @Test
    void putDeviceToken_returns204() {
        ResponseEntity<Void> resp = restTemplate.exchange(
            "/me/device-token", HttpMethod.PUT,
            new HttpEntity<>(Map.of("fcmToken", "fcm-test-token"), bearerHeaders()),
            Void.class
        );
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void getUserStatistics_self_returns200() {
        ResponseEntity<UserResponse> me = restTemplate.exchange(
            "/me", HttpMethod.GET,
            new HttpEntity<>(bearerHeaders()), UserResponse.class
        );
        Long userId = me.getBody().id();

        ResponseEntity<StatisticsResponse> resp = restTemplate.exchange(
            "/users/" + userId + "/statistics", HttpMethod.GET,
            new HttpEntity<>(bearerHeaders()), StatisticsResponse.class
        );

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().totalSessions()).isZero();
    }

    @Test
    void getUserStatistics_unknownUser_returns404() {
        ResponseEntity<String> resp = restTemplate.exchange(
            "/users/999999/statistics", HttpMethod.GET,
            new HttpEntity<>(bearerHeaders()), String.class
        );
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getUsersByCode_existingCode_returns200() {
        ResponseEntity<UserResponse> me = restTemplate.exchange(
            "/me", HttpMethod.GET,
            new HttpEntity<>(bearerHeaders()), UserResponse.class
        );
        String code = me.getBody().publicCode();

        ResponseEntity<UserSummaryResponse> resp = restTemplate.exchange(
            "/users/by-code/" + code, HttpMethod.GET,
            new HttpEntity<>(bearerHeaders()), UserSummaryResponse.class
        );

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().publicCode()).isEqualTo(code);
    }

    @Test
    void getUsersByCode_unknownCode_returns404() {
        ResponseEntity<String> resp = restTemplate.exchange(
            "/users/by-code/XXXXXXXX", HttpMethod.GET,
            new HttpEntity<>(bearerHeaders()), String.class
        );
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void deleteMe_returns204_thenGetMe_returns404() {
        ResponseEntity<Void> del = restTemplate.exchange(
            "/me", HttpMethod.DELETE,
            new HttpEntity<>(bearerHeaders()), Void.class
        );
        assertThat(del.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<String> get = restTemplate.exchange(
            "/me", HttpMethod.GET,
            new HttpEntity<>(bearerHeaders()), String.class
        );
        assertThat(get.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
