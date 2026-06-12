package com.runmvp.session.adapter.in.web;

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

class SessionLifecycleControllerIT extends BaseIntegrationTest {

    @Autowired private TestRestTemplate restTemplate;
    @MockitoBean private GoogleTokenVerifier googleTokenVerifier;

    private String tokenCreator;
    private String tokenGuest;
    private Long guestId;

    @BeforeEach
    void loginUsers() {
        String uid = UUID.randomUUID().toString().substring(0, 8);
        AuthResponse creator = login("tk-cr-" + uid, "sub-cr-" + uid, "cr-" + uid + "@x.com");
        AuthResponse guest   = login("tk-gs-" + uid, "sub-gs-" + uid, "gs-" + uid + "@x.com");
        tokenCreator = creator.accessToken();
        tokenGuest   = guest.accessToken();
        guestId      = guest.user().id();
    }

    private AuthResponse login(String idToken, String subject, String email) {
        when(googleTokenVerifier.verify(idToken)).thenReturn(
            new GoogleTokenVerifier.GoogleIdTokenPayload(subject, email, "Lifecycle User", null));
        return restTemplate.postForEntity(
            "/auth/google", Map.of("idToken", idToken), AuthResponse.class).getBody();
    }

    private HttpHeaders bearer(String token) {
        HttpHeaders h = new HttpHeaders();
        h.setBearerAuth(token);
        return h;
    }

    private Long createCoopSession() {
        var resp = restTemplate.exchange(
            "/sessions", HttpMethod.POST,
            new HttpEntity<>(Map.of("mode", "COOPERATIVE", "targetDistanceMeters", 5000,
                "invitedUserIds", java.util.List.of(guestId)), bearer(tokenCreator)),
            com.fasterxml.jackson.databind.JsonNode.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return resp.getBody().get("sessionId").asLong();
    }

    @Test
    void fullLifecycle_creatorStartsThenGuestAbandons() {
        Long sessionId = createCoopSession();

        // Guest accepts
        assertThat(restTemplate.exchange(
            "/sessions/" + sessionId + "/accept", HttpMethod.POST,
            new HttpEntity<>(bearer(tokenGuest)), Void.class)
            .getStatusCode()).isEqualTo(HttpStatus.OK);

        // Creator marks ready
        assertThat(restTemplate.exchange(
            "/sessions/" + sessionId + "/ready", HttpMethod.POST,
            new HttpEntity<>(bearer(tokenCreator)), Void.class)
            .getStatusCode()).isEqualTo(HttpStatus.OK);

        // Guest marks ready
        assertThat(restTemplate.exchange(
            "/sessions/" + sessionId + "/ready", HttpMethod.POST,
            new HttpEntity<>(bearer(tokenGuest)), Void.class)
            .getStatusCode()).isEqualTo(HttpStatus.OK);

        // Creator starts session
        assertThat(restTemplate.exchange(
            "/sessions/" + sessionId + "/start", HttpMethod.POST,
            new HttpEntity<>(bearer(tokenCreator)), Void.class)
            .getStatusCode()).isEqualTo(HttpStatus.OK);

        // Guest abandons
        assertThat(restTemplate.exchange(
            "/sessions/" + sessionId + "/abandon", HttpMethod.POST,
            new HttpEntity<>(bearer(tokenGuest)), Void.class)
            .getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void cancel_byCreator_returns200() {
        Long sessionId = createCoopSession();

        assertThat(restTemplate.exchange(
            "/sessions/" + sessionId + "/cancel", HttpMethod.POST,
            new HttpEntity<>(bearer(tokenCreator)), Void.class)
            .getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void cancel_byNonCreator_returns403() {
        Long sessionId = createCoopSession();

        ResponseEntity<String> resp = restTemplate.exchange(
            "/sessions/" + sessionId + "/cancel", HttpMethod.POST,
            new HttpEntity<>(bearer(tokenGuest)), String.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(resp.getBody()).contains("NOT_SESSION_CREATOR");
    }

    @Test
    void start_byNonCreator_returns403() {
        Long sessionId = createCoopSession();

        ResponseEntity<String> resp = restTemplate.exchange(
            "/sessions/" + sessionId + "/start", HttpMethod.POST,
            new HttpEntity<>(bearer(tokenGuest)), String.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(resp.getBody()).contains("NOT_SESSION_CREATOR");
    }

    @Test
    void start_alreadyActive_returns409() {
        Long sessionId = createCoopSession();

        // Start once
        restTemplate.exchange("/sessions/" + sessionId + "/start", HttpMethod.POST,
            new HttpEntity<>(bearer(tokenCreator)), Void.class);

        // Start again → conflict
        ResponseEntity<String> resp = restTemplate.exchange(
            "/sessions/" + sessionId + "/start", HttpMethod.POST,
            new HttpEntity<>(bearer(tokenCreator)), String.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(resp.getBody()).contains("INVALID_SESSION_STATE");
    }
}
