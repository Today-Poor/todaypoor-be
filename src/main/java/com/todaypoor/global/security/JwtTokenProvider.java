package com.todaypoor.global.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Component
public class JwtTokenProvider implements TokenProvider {

    private final String secretKeyPlain;
    private final long accessTokenExpirationTime;
    private final long refreshTokenExpirationTime;
    private SecretKey key;

    public JwtTokenProvider(
            @Value("${jwt.secret-key}") String secretKey,
            @Value("${jwt.access-expiration-time}") long accessTokenExpirationTime,
            @Value("${jwt.refresh-expiration-time}") long refreshTokenExpirationTime) {
        this.secretKeyPlain = secretKey;
        this.accessTokenExpirationTime = accessTokenExpirationTime;
        this.refreshTokenExpirationTime = refreshTokenExpirationTime;
    }

    @PostConstruct
    protected void init() {
        this.key = Keys.hmacShaKeyFor(secretKeyPlain.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String createAccessToken(UUID userId) {
        return createToken(userId, accessTokenExpirationTime);
    }

    @Override
    public String createRefreshToken(UUID userId) {
        return createToken(userId, refreshTokenExpirationTime);
    }

    private String createToken(UUID userId, long expirationTime) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + expirationTime);

        return Jwts.builder()
                .subject(userId.toString())
                .issuedAt(now)
                .expiration(validity)
                .signWith(key)
                .compact();
    }

    @Override
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.error("만료된 JWT 토큰입니다. (ExpiredJwtException)");
            throw e;
        } catch (SecurityException | MalformedJwtException | SignatureException e) {
            log.error("잘못된 JWT 서명입니다. (SignatureException/MalformedJwtException)");
            throw e;
        } catch (UnsupportedJwtException e) {
            log.error("지원되지 않는 JWT 토큰입니다. (UnsupportedJwtException)");
            throw e;
        } catch (IllegalArgumentException e) {
            log.error("JWT Claims 문자열이 비어있거나 올바르지 않습니다. (IllegalArgumentException)");
            throw e;
        }
    }

    @Override
    public UUID extractUserId(String token) {
        Claims claims = parseClaims(token);
        return UUID.fromString(claims.getSubject());
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
