package com.example.backend.mediator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private static final String SECRET = "test_secret_key_that_is_at_least_32_chars_long";

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET, 1_200_000L, 604_800_000L);
    }

    @Test
    void generateAccessToken_returnsNonBlankString() {
        assertThat(jwtService.generateAccessToken("alice")).isNotBlank();
    }

    @Test
    void generateRefreshToken_returnsNonBlankString() {
        assertThat(jwtService.generateRefreshToken("alice")).isNotBlank();
    }

    @Test
    void extractLogin_fromAccessToken_returnsOriginalLogin() {
        String login = "alice";
        String token = jwtService.generateAccessToken(login);
        assertThat(jwtService.extractLogin(token)).isEqualTo(login);
    }

    @Test
    void extractLogin_fromRefreshToken_returnsOriginalLogin() {
        String login = "bob";
        String token = jwtService.generateRefreshToken(login);
        assertThat(jwtService.extractLogin(token)).isEqualTo(login);
    }

    @Test
    void generateTokens_forSameLogin_accessAndRefreshAreDifferent() {
        String access  = jwtService.generateAccessToken("user");
        String refresh = jwtService.generateRefreshToken("user");
        assertThat(access).isNotEqualTo(refresh);
    }

    @Test
    void generateAccessToken_differentLogins_produceDifferentTokens() {
        String t1 = jwtService.generateAccessToken("alice");
        String t2 = jwtService.generateAccessToken("bob");
        assertThat(t1).isNotEqualTo(t2);
    }
}
