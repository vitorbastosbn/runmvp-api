package com.runmvp.auth.adapter.out.integration;

import com.runmvp.auth.application.port.out.JwtService;
import com.runmvp.shared.config.JwtProperties;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

@Component
class JwtServiceImpl implements JwtService {

    private final SecretKey key;
    private final long accessTokenExpirationMs;

    JwtServiceImpl(JwtProperties props) {
        byte[] keyBytes = Base64.getDecoder().decode(props.secret());
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.accessTokenExpirationMs = props.accessTokenExpirationSeconds() * 1_000L;
    }

    @Override
    public String generateAccessToken(Long userId) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
            .subject(String.valueOf(userId))
            .issuedAt(new Date(now))
            .expiration(new Date(now + accessTokenExpirationMs))
            .signWith(key)
            .compact();
    }

    @Override
    public Long extractUserId(String token) {
        String subject = Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .getPayload()
            .getSubject();
        return Long.parseLong(subject);
    }

    @Override
    public boolean isValid(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
