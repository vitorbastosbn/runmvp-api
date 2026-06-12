package com.runmvp.auth.adapter.in.web;

public record AuthResponse(
    String accessToken,
    String refreshToken,
    UserPayload user,
    boolean isNewUser
) {
    public record UserPayload(
        Long id,
        String name,
        String email,
        String avatarUrl,
        String publicCode,
        String entitlement
    ) {}
}
