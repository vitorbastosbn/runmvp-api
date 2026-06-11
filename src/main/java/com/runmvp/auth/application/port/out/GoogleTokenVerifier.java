package com.runmvp.auth.application.port.out;

public interface GoogleTokenVerifier {

    record GoogleIdTokenPayload(
        String subject,
        String email,
        String name,
        String pictureUrl
    ) {}

    GoogleIdTokenPayload verify(String idToken);
}
