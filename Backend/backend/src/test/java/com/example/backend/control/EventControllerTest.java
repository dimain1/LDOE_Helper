package com.example.backend.control;

import com.example.backend.dto.event.EventCreateRequest;
import com.example.backend.dto.event.EventDto;
import com.example.backend.dto.event.EventUpdateRequest;
import com.example.backend.mediator.EventService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class EventControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @Mock private EventService eventService;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new EventController(eventService))
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();

        // Устанавливаем аутентификацию в SecurityContext для @AuthenticationPrincipal
        var principal = User.withUsername("alice").password("").roles("USER").build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private EventDto sampleDto() {
        EventDto dto = new EventDto();
        dto.setId(10L);
        dto.setUserId(1L);
        dto.setName("Рейд");
        dto.setStartTime(Instant.parse("2026-06-05T10:00:00Z"));
        dto.setEndTime(Instant.parse("2026-06-05T11:00:00Z"));
        return dto;
    }

    // ── GET /events ────────────────────────────────────────────────────────────

    @Test
    void getMyEvents_authenticated_returnsEventList() throws Exception {
        when(eventService.getMyEvents("alice")).thenReturn(List.of(sampleDto()));

        mockMvc.perform(get("/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Рейд"))
                .andExpect(jsonPath("$[0].id").value(10));
    }

    // ── POST /events ───────────────────────────────────────────────────────────

    @Test
    void create_validRequest_returnsCreatedDto() throws Exception {
        when(eventService.create(eq("alice"), any())).thenReturn(sampleDto());

        EventCreateRequest req = new EventCreateRequest();
        req.setName("Рейд");
        req.setStartTime(Instant.parse("2026-06-05T10:00:00Z"));
        req.setEndTime(Instant.parse("2026-06-05T11:00:00Z"));

        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Рейд"));
    }

    // ── PUT /events/{id} ───────────────────────────────────────────────────────

    @Test
    void update_ownedEvent_returnsUpdatedDto() throws Exception {
        EventDto updated = sampleDto();
        updated.setName("Обновлённый рейд");
        when(eventService.update(eq("alice"), eq(10L), any())).thenReturn(updated);

        EventUpdateRequest req = new EventUpdateRequest();
        req.setName("Обновлённый рейд");

        mockMvc.perform(put("/events/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Обновлённый рейд"));
    }

    // ── DELETE /events/{id} ────────────────────────────────────────────────────

    @Test
    void delete_ownedEvent_returns200AndCallsService() throws Exception {
        mockMvc.perform(delete("/events/10"))
                .andExpect(status().isOk());

        verify(eventService).delete("alice", 10L);
    }
}
