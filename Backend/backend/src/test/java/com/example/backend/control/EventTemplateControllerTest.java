package com.example.backend.control;

import com.example.backend.dto.template.EventTemplateCreateRequest;
import com.example.backend.dto.template.EventTemplateDto;
import com.example.backend.dto.template.EventTemplateUpdateRequest;
import com.example.backend.mediator.EventTemplateService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockPart;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class EventTemplateControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock private EventTemplateService templateService;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new EventTemplateController(templateService))
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

    private EventTemplateDto sampleDto() {
        EventTemplateDto dto = new EventTemplateDto();
        dto.setId(7L);
        dto.setCreatorId(1L);
        dto.setName("Cooldown timer");
        dto.setDuration(3_600_000L);
        return dto;
    }

    // ── GET /templates ─────────────────────────────────────────────────────────

    @Test
    void getMyTemplates_authenticated_returnsTemplateList() throws Exception {
        when(templateService.getMyTemplates("alice")).thenReturn(List.of(sampleDto()));

        mockMvc.perform(get("/templates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Cooldown timer"))
                .andExpect(jsonPath("$[0].id").value(7));
    }

    // ── POST /templates (multipart) ────────────────────────────────────────────

    @Test
    void create_withoutImage_returnsCreatedDto() throws Exception {
        when(templateService.create(eq("alice"), any(), any())).thenReturn(sampleDto());

        EventTemplateCreateRequest req = new EventTemplateCreateRequest();
        req.setName("Cooldown timer");
        req.setDuration(3_600_000L);

        MockPart requestPart = new MockPart("request", objectMapper.writeValueAsBytes(req));
        requestPart.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(multipart("/templates").part(requestPart))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Cooldown timer"));
    }

    // ── PUT /templates/{id} (multipart) ────────────────────────────────────────

    @Test
    void update_ownedTemplate_returnsUpdatedDto() throws Exception {
        EventTemplateDto updated = sampleDto();
        updated.setName("New name");
        when(templateService.update(eq("alice"), eq(7L), any(), any())).thenReturn(updated);

        EventTemplateUpdateRequest req = new EventTemplateUpdateRequest();
        req.setName("New name");

        MockPart requestPart = new MockPart("request", objectMapper.writeValueAsBytes(req));
        requestPart.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(multipart("/templates/7")
                        .part(requestPart)
                        .with(r -> { r.setMethod("PUT"); return r; }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New name"));
    }

    // ── DELETE /templates/{id} ─────────────────────────────────────────────────

    @Test
    void delete_ownedTemplate_returns200AndCallsService() throws Exception {
        mockMvc.perform(delete("/templates/7"))
                .andExpect(status().isOk());

        verify(templateService).delete("alice", 7L);
    }
}
