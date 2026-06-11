package com.runmvp.auth.adapter.out.integration;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.runmvp.auth.application.port.out.GoogleTokenVerifier;
import com.runmvp.shared.config.GoogleAuthProperties;
import com.runmvp.shared.exception.BusinessException;
import com.runmvp.shared.exception.ErrorCode;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
class GoogleTokenVerifierAdapter implements GoogleTokenVerifier {

    private final GoogleIdTokenVerifier googleVerifier;

    GoogleTokenVerifierAdapter(GoogleIdTokenVerifier googleVerifier,
                               GoogleAuthProperties props) {
        this.googleVerifier = googleVerifier;
    }

    @Override
    public GoogleIdTokenPayload verify(String idToken) {
        try {
            GoogleIdToken token = googleVerifier.verify(idToken);
            if (token == null) {
                throw new BusinessException(ErrorCode.GOOGLE_TOKEN_INVALID);
            }
            GoogleIdToken.Payload p = token.getPayload();
            return new GoogleIdTokenPayload(
                p.getSubject(),
                p.getEmail(),
                (String) p.get("name"),
                (String) p.get("picture")
            );
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.GOOGLE_TOKEN_INVALID, e.getMessage());
        }
    }
}

@Configuration
class GoogleVerifierConfig {

    @Bean
    GoogleIdTokenVerifier googleIdTokenVerifier(GoogleAuthProperties props) {
        return new GoogleIdTokenVerifier.Builder(
            new NetHttpTransport(),
            GsonFactory.getDefaultInstance()
        )
        .setAudience(Collections.singletonList(props.clientId()))
        .build();
    }
}
