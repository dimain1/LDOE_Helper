package com.example.backend.control;

import com.example.backend.dto.auth.AuthResponse;
import com.example.backend.dto.auth.LoginRequest;
import com.example.backend.dto.auth.RefreshRequest;
import com.example.backend.dto.auth.RegisterRequest;
import com.example.backend.entity.User;
import com.example.backend.entity.UserRole;
import com.example.backend.mediator.AuthService;
import com.example.backend.mediator.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock private AuthService authService;
    @Mock private JwtService jwtService;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new AuthController(authService, jwtService))
                .build();
    }

    // ── /auth/register ─────────────────────────────────────────────────────────

    @Test
    void register_validRequest_returns200WithUserId() throws Exception {
        UserRole role = new UserRole();
        role.setName("USER");
        User saved = new User("alice", "hashed", "alice@mail.com", role);
        saved.setId(1L);
        when(authService.register(any())).thenReturn(saved);

        RegisterRequest req = new RegisterRequest();
        req.setLogin("alice");
        req.setPassword("secret");
        req.setEmail("alice@mail.com");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(content().string("1"));
    }

    // ── /auth/login ────────────────────────────────────────────────────────────

    @Test
    void login_validCredentials_returnsTokenPair() throws Exception {
        when(authService.login(any())).thenReturn(new AuthResponse("acc-token", "ref-token"));

        LoginRequest req = new LoginRequest();
        req.setLogin("alice");
        req.setPassword("secret");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("acc-token"))
                .andExpect(jsonPath("$.refreshToken").value("ref-token"));
    }

    @Test
    void login_wrongCredentials_returns401() throws Exception {
        when(authService.login(any()))
                .thenThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Неверный логин или пароль"));

        LoginRequest req = new LoginRequest();
        req.setLogin("alice");
        req.setPassword("wrong");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    // ── /auth/logout ───────────────────────────────────────────────────────────

    @Test
    void logout_validToken_returns200AndCallsService() throws Exception {
        RefreshRequest req = new RefreshRequest();
        req.setRefreshToken("some-token");

        mockMvc.perform(post("/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        verify(authService).logout(any());
    }

    // ── /auth/refresh ──────────────────────────────────────────────────────────

    @Test
    void refresh_validToken_returnsNewTokenPair() throws Exception {
        when(authService.refresh(any())).thenReturn(new AuthResponse("new-acc", "new-ref"));

        RefreshRequest req = new RefreshRequest();
        req.setRefreshToken("old-ref");

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-acc"))
                .andExpect(jsonPath("$.refreshToken").value("new-ref"));
    }
}
