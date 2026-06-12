package com.runmvp.shared.security;

import com.runmvp.BaseIntegrationTest;
import com.runmvp.auth.application.port.out.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class JwtFilterIT extends BaseIntegrationTest {

    @Autowired private TestRestTemplate restTemplate;
    @Autowired private JwtService jwtService;

    @Test
    void getMe_withoutToken_returns401() {
        ResponseEntity<String> response = restTemplate.getForEntity("/me", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void getMe_withValidToken_doesNotReturn401() {
        String token = jwtService.generateAccessToken(999L);
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        ResponseEntity<String> response = restTemplate.exchange(
            "/me",
            HttpMethod.GET,
            new HttpEntity<>(headers),
            String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getMe_withInvalidToken_returns401() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer invalid.jwt.token");

        ResponseEntity<String> response = restTemplate.exchange(
            "/me",
            HttpMethod.GET,
            new HttpEntity<>(headers),
            String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
