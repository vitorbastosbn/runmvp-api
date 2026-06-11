package com.runmvp.auth.adapter.out.integration;

import com.runmvp.shared.config.JwtProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class JwtServiceImplTest {

    private static final String SECRET =
        "dGVzdC1zZWNyZXQtY2hhdmUtcGFyYS10ZXN0ZXMtbG9uZ2Etc3VmaWNpZW50ZS0zMmJ5dGVz";

    private JwtServiceImpl jwtService;

    @BeforeEach
    void setUp() {
        JwtProperties props = new JwtProperties(SECRET, 900L, 30L);
        jwtService = new JwtServiceImpl(props);
    }

    @Test
    void generateAndExtract_returnsCorrectUserId() {
        String token = jwtService.generateAccessToken(42L);
        assertThat(jwtService.extractUserId(token)).isEqualTo(42L);
    }

    @Test
    void isValid_trueForFreshToken() {
        String token = jwtService.generateAccessToken(1L);
        assertThat(jwtService.isValid(token)).isTrue();
    }

    @Test
    void isValid_falseForGarbage() {
        assertThat(jwtService.isValid("not.a.jwt")).isFalse();
    }

    @Test
    void isValid_falseForTokenSignedWithDifferentKey() {
        JwtProperties otherProps = new JwtProperties(
            "b3RoZXItc2VjcmV0LWtleS1mb3ItdGVzdGluZy1wdXJwb3Nlcy1vbmx5MDM=",
            900L, 30L);
        JwtServiceImpl other = new JwtServiceImpl(otherProps);
        String foreignToken = other.generateAccessToken(1L);
        assertThat(jwtService.isValid(foreignToken)).isFalse();
    }

    @Test
    void extractUserId_throwsForInvalidToken() {
        assertThatThrownBy(() -> jwtService.extractUserId("bad"))
            .isInstanceOf(Exception.class);
    }
}
