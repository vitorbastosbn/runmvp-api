package com.runmvp.session.adapter.in.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.runmvp.BaseIntegrationTest;
import com.runmvp.auth.adapter.in.web.AuthResponse;
import com.runmvp.auth.application.port.out.GoogleTokenVerifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class SessionCreateControllerIT extends BaseIntegrationTest {

    @Autowired private TestRestTemplate restTemplate;
    @MockitoBean private GoogleTokenVerifier googleTokenVerifier;

    private String tokenCreator;
    private String tokenInvitee;
    private String tokenOutsider;
    private Long inviteeId;

    @BeforeEach
    void loginUsers() {
        String unique = UUID.randomUUID().toString().substring(0, 8);
        AuthResponse creator = login("tk-c-" + unique, "sub-c-" + unique, "c-" + unique + "@x.com");
        AuthResponse invitee = login("tk-i-" + unique, "sub-i-" + unique, "i-" + unique + "@x.com");
        AuthResponse outsider = login("tk-o-" + unique, "sub-o-" + unique, "o-" + unique + "@x.com");
        tokenCreator = creator.accessToken();
        tokenInvitee = invitee.accessToken();
        tokenOutsider = outsider.accessToken();
        inviteeId = invitee.user().id();
    }

    private AuthResponse login(String idToken, String subject, String email) {
        when(googleTokenVerifier.verify(idToken)).thenReturn(
            new GoogleTokenVerifier.GoogleIdTokenPayload(subject, email, "Session User", null));
        return restTemplate.postForEntity(
            "/auth/google", Map.of("idToken", idToken), AuthResponse.class).getBody();
    }

    private HttpHeaders bearer(String token) {
        HttpHeaders h = new HttpHeaders();
        h.setBearerAuth(token);
        return h;
    }

    private Long createSessionWithInvitee() {
        ResponseEntity<JsonNode> resp = restTemplate.exchange(
            "/sessions", HttpMethod.POST,
            new HttpEntity<>(Map.of(
                "mode", "COOPERATIVE",
                "targetDistanceMeters", 5000,
                "invitedUserIds", List.of(inviteeId)), bearer(tokenCreator)),
            JsonNode.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return resp.getBody().get("sessionId").asLong();
    }

    @Test
    void createSession_returns201WithSessionId() {
        Long sessionId = createSessionWithInvitee();
        assertThat(sessionId).isPositive();
    }

    @Test
    void acceptInvite_byInvitee_returns200() {
        Long sessionId = createSessionWithInvitee();

        ResponseEntity<Void> resp = restTemplate.exchange(
            "/sessions/" + sessionId + "/accept", HttpMethod.POST,
            new HttpEntity<>(bearer(tokenInvitee)), Void.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void declineInvite_byNonInvited_returns404() {
        Long sessionId = createSessionWithInvitee();

        ResponseEntity<String> resp = restTemplate.exchange(
            "/sessions/" + sessionId + "/decline", HttpMethod.POST,
            new HttpEntity<>(bearer(tokenOutsider)), String.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(resp.getBody()).contains("NOT_SESSION_PARTICIPANT");
    }

    @Test
    void invite_byCreator_returns200_duplicateReturns409() {
        ResponseEntity<JsonNode> created = restTemplate.exchange(
            "/sessions", HttpMethod.POST,
            new HttpEntity<>(Map.of("mode", "COOPERATIVE", "targetDistanceMeters", 5000),
                bearer(tokenCreator)),
            JsonNode.class);
        Long sessionId = created.getBody().get("sessionId").asLong();

        ResponseEntity<Void> invite = restTemplate.exchange(
            "/sessions/" + sessionId + "/invite/" + inviteeId, HttpMethod.POST,
            new HttpEntity<>(bearer(tokenCreator)), Void.class);
        assertThat(invite.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<String> dup = restTemplate.exchange(
            "/sessions/" + sessionId + "/invite/" + inviteeId, HttpMethod.POST,
            new HttpEntity<>(bearer(tokenCreator)), String.class);
        assertThat(dup.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(dup.getBody()).contains("ALREADY_SESSION_PARTICIPANT");
    }

    @Test
    void invite_byNonCreator_returns404() {
        Long sessionId = createSessionWithInvitee();

        ResponseEntity<String> resp = restTemplate.exchange(
            "/sessions/" + sessionId + "/invite/" + inviteeId, HttpMethod.POST,
            new HttpEntity<>(bearer(tokenOutsider)), String.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void createSession_noToken_returns401() {
        ResponseEntity<String> resp = restTemplate.postForEntity(
            "/sessions", Map.of("mode", "COMPETITIVE"), String.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void createCompetitiveSession_asFreeUser_returns403() {
        ResponseEntity<String> resp = restTemplate.exchange(
            "/sessions", HttpMethod.POST,
            new HttpEntity<>(Map.of("mode", "COMPETITIVE", "targetDistanceMeters", 5000),
                bearer(tokenCreator)),
            String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(resp.getBody()).contains("PREMIUM_REQUIRED");
    }
}
