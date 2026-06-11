package com.runmvp.auth.adapter.out.integration;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.runmvp.auth.application.port.out.GoogleTokenVerifier;
import com.runmvp.shared.config.GoogleAuthProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoogleTokenVerifierAdapterTest {

    @Mock
    private GoogleIdTokenVerifier googleVerifier;

    private GoogleTokenVerifierAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new GoogleTokenVerifierAdapter(
            googleVerifier,
            new GoogleAuthProperties("test-client-id")
        );
    }

    @Test
    void verify_validToken_returnsPayload() throws Exception {
        GoogleIdToken.Payload payload = new GoogleIdToken.Payload();
        payload.setSubject("google-sub-abc");
        payload.setEmail("user@example.com");
        payload.set("name", "Test User");
        payload.set("picture", "https://avatar.example.com/img.jpg");

        GoogleIdToken mockToken = mock(GoogleIdToken.class);
        when(mockToken.getPayload()).thenReturn(payload);
        when(googleVerifier.verify("valid-token")).thenReturn(mockToken);

        GoogleTokenVerifier.GoogleIdTokenPayload result = adapter.verify("valid-token");

        assertThat(result.subject()).isEqualTo("google-sub-abc");
        assertThat(result.email()).isEqualTo("user@example.com");
        assertThat(result.name()).isEqualTo("Test User");
        assertThat(result.pictureUrl()).isEqualTo("https://avatar.example.com/img.jpg");
    }

    @Test
    void verify_nullReturnedByGoogle_throwsBusinessException() throws Exception {
        when(googleVerifier.verify("bad-token")).thenReturn(null);
        assertThatThrownBy(() -> adapter.verify("bad-token"))
            .hasMessageContaining("GOOGLE_TOKEN_INVALID");
    }

    @Test
    void verify_exceptionFromGoogle_throwsBusinessException() throws Exception {
        when(googleVerifier.verify(anyString())).thenThrow(new RuntimeException("network"));
        assertThatThrownBy(() -> adapter.verify("any-token"))
            .hasMessageContaining("GOOGLE_TOKEN_INVALID");
    }
}
