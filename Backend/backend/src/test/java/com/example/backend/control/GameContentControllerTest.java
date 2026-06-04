package com.example.backend.control;

import com.example.backend.dto.gameContent.ContentTypeDto;
import com.example.backend.dto.gameContent.GameContentDto;
import com.example.backend.dto.gameContent.GameContentRequest;
import com.example.backend.mediator.GameContentService;
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
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Примечание: @PreAuthorize (admin-only) не проверяется в standalone-тестах.
// Метод-уровневая безопасность требует полного Spring Security контекста.
@ExtendWith(MockitoExtension.class)
class GameContentControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock private GameContentService gameContentService;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new GameContentController(gameContentService))
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

    private GameContentDto sampleContentDto() {
        GameContentDto dto = new GameContentDto();
        dto.setId(100L);
        dto.setName("АК-74");
        dto.setTypes(Set.of(new ContentTypeDto(1L, "All")));
        dto.setPinned(false);
        return dto;
    }

    // ── GET /content ───────────────────────────────────────────────────────────

    @Test
    void getAll_authenticated_returnsContentList() throws Exception {
        when(gameContentService.getAll("alice")).thenReturn(List.of(sampleContentDto()));

        mockMvc.perform(get("/content"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("АК-74"))
                .andExpect(jsonPath("$[0].id").value(100));
    }

    // ── POST /content (multipart) ──────────────────────────────────────────────

    @Test
    void create_validRequest_returnsCreatedDto() throws Exception {
        when(gameContentService.create(any(), any())).thenReturn(sampleContentDto());

        GameContentRequest req = new GameContentRequest();
        req.setName("АК-74");

        MockPart requestPart = new MockPart("request", objectMapper.writeValueAsBytes(req));
        requestPart.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(multipart("/content").part(requestPart))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("АК-74"));
    }

    // ── GET /content/types ─────────────────────────────────────────────────────

    @Test
    void getAllTypes_returnsTypeList() throws Exception {
        when(gameContentService.getAllTypes()).thenReturn(
                List.of(new ContentTypeDto(1L, "All"), new ContentTypeDto(2L, "Weapons")));

        mockMvc.perform(get("/content/types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("All"))
                .andExpect(jsonPath("$[1].name").value("Weapons"));
    }

    // ── POST /content/types ────────────────────────────────────────────────────

    @Test
    void createType_validName_returnsCreatedType() throws Exception {
        when(gameContentService.createType("Weapons"))
                .thenReturn(new ContentTypeDto(2L, "Weapons"));

        mockMvc.perform(post("/content/types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Weapons\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Weapons"))
                .andExpect(jsonPath("$.id").value(2));
    }

    // ── DELETE /content/types/{id} ─────────────────────────────────────────────

    @Test
    void deleteType_validId_returns200AndCallsService() throws Exception {
        mockMvc.perform(delete("/content/types/2"))
                .andExpect(status().isOk());

        verify(gameContentService).deleteType(2L);
    }

    // ── POST /content/{id}/pin ─────────────────────────────────────────────────

    @Test
    void pin_authenticated_returns200AndCallsService() throws Exception {
        mockMvc.perform(post("/content/100/pin"))
                .andExpect(status().isOk());

        verify(gameContentService).pin("alice", 100L);
    }

    // ── DELETE /content/{id}/pin ───────────────────────────────────────────────

    @Test
    void unpin_authenticated_returns200AndCallsService() throws Exception {
        mockMvc.perform(delete("/content/100/pin"))
                .andExpect(status().isOk());

        verify(gameContentService).unpin("alice", 100L);
    }
}
