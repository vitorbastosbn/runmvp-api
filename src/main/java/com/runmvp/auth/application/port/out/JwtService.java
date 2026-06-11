package com.runmvp.auth.application.port.out;

public interface JwtService {
    String generateAccessToken(Long userId);
    Long extractUserId(String token);
    boolean isValid(String token);
}
