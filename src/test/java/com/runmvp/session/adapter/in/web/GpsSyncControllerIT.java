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

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class GpsSyncControllerIT extends BaseIntegrationTest {

    @Autowired private TestRestTemplate restTemplate;
    @MockitoBean private GoogleTokenVerifier googleTokenVerifier;

    private String tokenCreator;
    private Long sessionId;

    @BeforeEach
    void setup() {
        String uid = UUID.randomUUID().toString().substring(0, 8);
        AuthResponse creator = login("tk-gps-" + uid, "sub-gps-" + uid, "gps-" + uid + "@x.com");
        tokenCreator = creator.accessToken();

        // Create and start a session so it is ACTIVE
        var created = restTemplate.exchange(
            "/sessions", HttpMethod.POST,
            new HttpEntity<>(Map.of("mode", "COOPERATIVE", "targetDistanceMeters", 5000),
                bearer(tokenCreator)),
            com.fasterxml.jackson.databind.JsonNode.class);
        sessionId = created.getBody().get("sessionId").asLong();

        restTemplate.exchange("/sessions/" + sessionId + "/start", HttpMethod.POST,
            new HttpEntity<>(bearer(tokenCreator)), Void.class);
    }

    private AuthResponse login(String idToken, String subject, String email) {
        when(googleTokenVerifier.verify(idToken)).thenReturn(
            new GoogleTokenVerifier.GoogleIdTokenPayload(subject, email, "GPS User", null));
        return restTemplate.postForEntity(
            "/auth/google", Map.of("idToken", idToken), AuthResponse.class).getBody();
    }

    private HttpHeaders bearer(String token) {
        HttpHeaders h = new HttpHeaders();
        h.setBearerAuth(token);
        return h;
    }

    private List<Map<String, Object>> twoPoints() {
        return List.of(
            Map.of("sequence", 0, "latitude", -23.5, "longitude", -46.6,
                "accuracyMeters", 5.0, "speedMps", 2.5, "isMocked", false,
                "capturedAt", Instant.now().toString()),
            Map.of("sequence", 1, "latitude", -23.51, "longitude", -46.61,
                "accuracyMeters", 4.0, "speedMps", 2.6, "isMocked", false,
                "capturedAt", Instant.now().toString())
        );
    }

    @Test
    void activity_validBatch_returns200() {
        ResponseEntity<Void> resp = restTemplate.exchange(
            "/sessions/" + sessionId + "/activity", HttpMethod.POST,
            new HttpEntity<>(Map.of("points", twoPoints()), bearer(tokenCreator)),
            Void.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void activity_sameBatchTwice_idempotentReturns200() {
        var body = new HttpEntity<>(Map.of("points", twoPoints()), bearer(tokenCreator));
        restTemplate.exchange("/sessions/" + sessionId + "/activity", HttpMethod.POST, body, Void.class);
        ResponseEntity<Void> resp = restTemplate.exchange(
            "/sessions/" + sessionId + "/activity", HttpMethod.POST, body, Void.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void activity_mockedPoints_returns200() {
        var mockedPoint = Map.of("sequence", 0, "latitude", -23.5, "longitude", -46.6,
            "accuracyMeters", 5.0, "speedMps", 2.5, "isMocked", true,
            "capturedAt", Instant.now().toString());
        ResponseEntity<Void> resp = restTemplate.exchange(
            "/sessions/" + sessionId + "/activity", HttpMethod.POST,
            new HttpEntity<>(Map.of("points", List.of(mockedPoint)), bearer(tokenCreator)),
            Void.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void activity_noToken_returns401() {
        ResponseEntity<String> resp = restTemplate.postForEntity(
            "/sessions/" + sessionId + "/activity",
            Map.of("points", List.of()), String.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
