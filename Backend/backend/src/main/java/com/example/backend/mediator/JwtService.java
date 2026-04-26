package com.example.backend.mediator;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtService {

    private static final String SECRET =
            "my_super_secret_key_which_must_be_at_least_32_chars_long";

    private final SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes());

    private final long ACCESS_EXPIRATION = 1000 * 60 * 20; // 20 minutes;

    private final long REFRESH_EXPIRATION = 1000 * 60 * 60 * 24 * 7; // 7 days;



    public String generateAccessToken(String login) {
        return Jwts.builder()
                .setSubject(login)
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_EXPIRATION))
                .signWith(key)
                .compact();
    }

public String generateRefreshToken(String login) {
    return Jwts.builder()
            .setSubject(login)
            .setExpiration(new Date(System.currentTimeMillis() + REFRESH_EXPIRATION))
            .signWith(key)
            .compact();
    }

    public String extractLogin(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}