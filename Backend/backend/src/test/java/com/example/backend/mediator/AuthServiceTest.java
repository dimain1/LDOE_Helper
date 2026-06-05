package com.example.backend.mediator;

import com.example.backend.dto.auth.AuthResponse;
import com.example.backend.dto.auth.LoginRequest;
import com.example.backend.dto.auth.RefreshRequest;
import com.example.backend.dto.auth.RegisterRequest;
import com.example.backend.entity.RefreshToken;
import com.example.backend.entity.User;
import com.example.backend.entity.UserRole;
import com.example.backend.foundation.repository.RefreshTokenRepository;
import com.example.backend.foundation.repository.UserRepository;
import com.example.backend.foundation.repository.UserRoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Spy
    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserRoleRepository userRoleRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private AuthService authService;

    private User user;
    private UserRole role;
    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @BeforeEach
    void setUp() {
        role = new UserRole();
        role.setId(1L);
        role.setName("USER");

        user = new User("alice", encoder.encode("secret"), "alice@mail.com", role);
        user.setId(1L);
    }

    // ── login ──────────────────────────────────────────────────────────────────

    @Test
    void login_validCredentials_returnsAccessAndRefreshTokens() {
        when(userRepository.findByLogin("alice")).thenReturn(Optional.of(user));
        when(jwtService.generateAccessToken("alice")).thenReturn("access-token");
        when(jwtService.generateRefreshToken("alice")).thenReturn("refresh-token");
        when(refreshTokenRepository.save(any())).thenReturn(new RefreshToken());

        LoginRequest req = new LoginRequest();
        req.setLogin("alice");
        req.setPassword("secret");

        AuthResponse response = authService.login(req);

        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void login_wrongPassword_throwsUnauthorized() {
        when(userRepository.findByLogin("alice")).thenReturn(Optional.of(user));

        LoginRequest req = new LoginRequest();
        req.setLogin("alice");
        req.setPassword("wrong-password");

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex ->
                        assertThat(((ResponseStatusException) ex).getStatusCode())
                                .isEqualTo(HttpStatus.UNAUTHORIZED));
    }

    @Test
    void login_unknownUser_throwsUnauthorized() {
        when(userRepository.findByLogin("ghost")).thenReturn(Optional.empty());

        LoginRequest req = new LoginRequest();
        req.setLogin("ghost");
        req.setPassword("any");

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex ->
                        assertThat(((ResponseStatusException) ex).getStatusCode())
                                .isEqualTo(HttpStatus.UNAUTHORIZED));
    }

    // ── register ───────────────────────────────────────────────────────────────

    @Test
    void register_newLogin_savesUserAndReturnsIt() {
        when(userRepository.findByLogin("newUser")).thenReturn(Optional.empty());
        when(userRoleRepository.findByName("ADMIN")).thenReturn(Optional.of(role));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        RegisterRequest req = new RegisterRequest();
        req.setLogin("newUser");
        req.setPassword("pass");
        req.setEmail("new@mail.com");

        User saved = authService.register(req);

        assertThat(saved.getLogin()).isEqualTo("newUser");
        assertThat(saved.getEmail()).isEqualTo("new@mail.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_duplicateLogin_throwsConflict() {
        when(userRepository.findByLogin("alice")).thenReturn(Optional.of(user));

        RegisterRequest req = new RegisterRequest();
        req.setLogin("alice");
        req.setPassword("pass");
        req.setEmail("other@mail.com");

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex ->
                        assertThat(((ResponseStatusException) ex).getStatusCode())
                                .isEqualTo(HttpStatus.CONFLICT));
    }

    @Test
    void register_defaultRoleNotFound_throwsInternalServerError() {
        when(userRepository.findByLogin("newUser")).thenReturn(Optional.empty());

        RegisterRequest req = new RegisterRequest();
        req.setLogin("newUser");
        req.setPassword("pass");
        req.setEmail("new@mail.com");

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex ->
                        assertThat(((ResponseStatusException) ex).getStatusCode())
                                .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    // ── logout ─────────────────────────────────────────────────────────────────

    @Test
    void logout_callsDeleteByToken() {
        RefreshRequest req = new RefreshRequest();
        req.setRefreshToken("some-token");

        authService.logout(req);

        verify(refreshTokenRepository).deleteByToken("some-token");
    }

    // ── refresh ────────────────────────────────────────────────────────────────

    @Test
    void refresh_validToken_returnsNewTokenPair() {
        RefreshToken stored = new RefreshToken(user, "old-token",
                Instant.now().plusSeconds(3600));

        when(refreshTokenRepository.findByToken("old-token")).thenReturn(Optional.of(stored));
        when(jwtService.generateAccessToken("alice")).thenReturn("new-access");
        when(jwtService.generateRefreshToken("alice")).thenReturn("new-refresh");
        when(refreshTokenRepository.save(any())).thenReturn(stored);

        RefreshRequest req = new RefreshRequest();
        req.setRefreshToken("old-token");

        AuthResponse response = authService.refresh(req);

        assertThat(response.getAccessToken()).isEqualTo("new-access");
        assertThat(response.getRefreshToken()).isEqualTo("new-refresh");
        verify(refreshTokenRepository).save(stored);
    }

    @Test
    void refresh_tokenNotFound_throwsUnauthorized() {
        when(refreshTokenRepository.findByToken("bad-token")).thenReturn(Optional.empty());

        RefreshRequest req = new RefreshRequest();
        req.setRefreshToken("bad-token");

        assertThatThrownBy(() -> authService.refresh(req))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex ->
                        assertThat(((ResponseStatusException) ex).getStatusCode())
                                .isEqualTo(HttpStatus.UNAUTHORIZED));
    }

    @Test
    void refresh_expiredToken_throwsUnauthorizedAndDeletesToken() {
        RefreshToken expired = new RefreshToken(user, "expired-token",
                Instant.now().minusSeconds(60));

        when(refreshTokenRepository.findByToken("expired-token")).thenReturn(Optional.of(expired));

        RefreshRequest req = new RefreshRequest();
        req.setRefreshToken("expired-token");

        assertThatThrownBy(() -> authService.refresh(req))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex ->
                        assertThat(((ResponseStatusException) ex).getStatusCode())
                                .isEqualTo(HttpStatus.UNAUTHORIZED));

        verify(refreshTokenRepository).delete(expired);
    }
}
