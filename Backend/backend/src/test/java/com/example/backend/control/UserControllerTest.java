package com.example.backend.control;

import com.example.backend.dto.user.ChangePasswordRequest;
import com.example.backend.dto.user.UpdateUserRequest;
import com.example.backend.dto.user.UserDto;
import com.example.backend.mediator.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock private UserService userService;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new UserController(userService))
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();

        var principal = User.withUsername("alice").password("").roles("USER").build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // ── GET /users/me ──────────────────────────────────────────────────────────

    @Test
    void getMe_authenticated_returnsUserDto() throws Exception {
        when(userService.getMe("alice")).thenReturn(new UserDto(1L, "alice", "alice@mail.com", false));

        mockMvc.perform(get("/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.login").value("alice"))
                .andExpect(jsonPath("$.email").value("alice@mail.com"))
                .andExpect(jsonPath("$.admin").value(false));
    }

    // ── PUT /users/me ──────────────────────────────────────────────────────────

    @Test
    void updateMe_validRequest_returnsUpdatedDto() throws Exception {
        when(userService.updateMe(eq("alice"), any()))
                .thenReturn(new UserDto(1L, "alice_new", "new@mail.com", false));

        UpdateUserRequest req = new UpdateUserRequest();
        req.setLogin("alice_new");
        req.setEmail("new@mail.com");

        mockMvc.perform(put("/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.login").value("alice_new"));
    }

    // ── PUT /users/me/password ─────────────────────────────────────────────────

    @Test
    void changePassword_validRequest_returns200AndCallsService() throws Exception {
        ChangePasswordRequest req = new ChangePasswordRequest();
        req.setOldPassword("oldPass");
        req.setNewPassword("newPass");

        mockMvc.perform(put("/users/me/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        verify(userService).changePassword(eq("alice"), any());
    }
}
