package com.example.backend.mediator;

import com.example.backend.dto.template.EventTemplateCreateRequest;
import com.example.backend.dto.template.EventTemplateDto;
import com.example.backend.dto.template.EventTemplateUpdateRequest;
import com.example.backend.entity.EventTemplate;
import com.example.backend.entity.User;
import com.example.backend.entity.UserRole;
import com.example.backend.foundation.repository.EventTemplateRepository;
import com.example.backend.foundation.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventTemplateServiceTest {

    @Mock
    private EventTemplateRepository templateRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ImageStorageService imageStorageService;

    @InjectMocks
    private EventTemplateService templateService;

    private User user;
    private EventTemplate template;

    @BeforeEach
    void setUp() {
        UserRole role = new UserRole();
        role.setName("USER");

        user = new User("bob", "hashed", "bob@mail.com", role);
        user.setId(2L);

        template = new EventTemplate();
        template.setId(7L);
        template.setCreator(user);
        template.setName("Cooldown timer");
        template.setDuration(1800L);
    }

    // ── getMyTemplates ─────────────────────────────────────────────────────────

    @Test
    void getMyTemplates_returnsCreatorTemplates() {
        when(userRepository.findByLogin("bob")).thenReturn(Optional.of(user));
        when(templateRepository.findByCreator(user)).thenReturn(List.of(template));

        List<EventTemplateDto> result = templateService.getMyTemplates("bob");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Cooldown timer");
    }

    @Test
    void getMyTemplates_unknownLogin_throws() {
        when(userRepository.findByLogin("nobody")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> templateService.getMyTemplates("nobody"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found");
    }

    // ── create ─────────────────────────────────────────────────────────────────

    @Test
    void create_withoutImage_savesTemplateAndReturnsDto() {
        when(userRepository.findByLogin("bob")).thenReturn(Optional.of(user));
        when(templateRepository.save(any())).thenReturn(template);

        EventTemplateCreateRequest req = new EventTemplateCreateRequest();
        req.setName("Cooldown timer");
        req.setDescription("desc");
        req.setDuration(1800L);

        EventTemplateDto dto = templateService.create("bob", req, null);

        assertThat(dto.getName()).isEqualTo("Cooldown timer");
        verify(imageStorageService, never()).saveImage(any());
    }

    @Test
    void create_withImage_callsImageStorage() {
        when(userRepository.findByLogin("bob")).thenReturn(Optional.of(user));
        when(imageStorageService.saveImage(any())).thenReturn("/images/timer.png");
        when(templateRepository.save(any())).thenReturn(template);

        MockMultipartFile image = new MockMultipartFile(
                "image", "timer.png", "image/png", new byte[]{1, 2, 3});

        EventTemplateCreateRequest req = new EventTemplateCreateRequest();
        req.setName("Cooldown timer");
        req.setDuration(1800L);

        templateService.create("bob", req, image);

        verify(imageStorageService).saveImage(image);
    }

    @Test
    void create_withEmptyMultipartFile_doesNotCallImageStorage() {
        when(userRepository.findByLogin("bob")).thenReturn(Optional.of(user));
        when(templateRepository.save(any())).thenReturn(template);

        MockMultipartFile emptyFile = new MockMultipartFile(
                "image", "timer.png", "image/png", new byte[0]);

        EventTemplateCreateRequest req = new EventTemplateCreateRequest();
        req.setName("Cooldown timer");
        req.setDuration(1800L);

        templateService.create("bob", req, emptyFile);

        verify(imageStorageService, never()).saveImage(any());
    }

    // ── update ─────────────────────────────────────────────────────────────────

    @Test
    void update_ownedTemplate_updatesName() {
        when(templateRepository.findById(7L)).thenReturn(Optional.of(template));
        when(templateRepository.save(any())).thenReturn(template);

        EventTemplateUpdateRequest req = new EventTemplateUpdateRequest();
        req.setName("New name");

        templateService.update("bob", 7L, req, null);

        assertThat(template.getName()).isEqualTo("New name");
    }

    @Test
    void update_ownedTemplate_updatesDuration() {
        when(templateRepository.findById(7L)).thenReturn(Optional.of(template));
        when(templateRepository.save(any())).thenReturn(template);

        EventTemplateUpdateRequest req = new EventTemplateUpdateRequest();
        req.setDuration(3600L);

        templateService.update("bob", 7L, req, null);

        assertThat(template.getDuration()).isEqualTo(3600L);
    }

    @Test
    void update_nullFields_doesNotOverwriteExistingValues() {
        when(templateRepository.findById(7L)).thenReturn(Optional.of(template));
        when(templateRepository.save(any())).thenReturn(template);

        templateService.update("bob", 7L, new EventTemplateUpdateRequest(), null);

        assertThat(template.getName()).isEqualTo("Cooldown timer");
    }

    @Test
    void update_templateNotFound_throws() {
        when(templateRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> templateService.update("bob", 99L,
                new EventTemplateUpdateRequest(), null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Template not found");
    }

    @Test
    void update_callerIsNotCreator_throwsAccessDenied() {
        when(templateRepository.findById(7L)).thenReturn(Optional.of(template));

        assertThatThrownBy(() -> templateService.update("alice", 7L,
                new EventTemplateUpdateRequest(), null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Access denied");
    }

    @Test
    void update_withImage_callsImageStorage() {
        when(templateRepository.findById(7L)).thenReturn(Optional.of(template));
        when(imageStorageService.saveImage(any())).thenReturn("/images/new.png");
        when(templateRepository.save(any())).thenReturn(template);

        MockMultipartFile image = new MockMultipartFile(
                "image", "new.png", "image/png", new byte[]{9, 8, 7});

        templateService.update("bob", 7L, new EventTemplateUpdateRequest(), image);

        verify(imageStorageService).saveImage(image);
    }

    // ── delete ─────────────────────────────────────────────────────────────────

    @Test
    void delete_ownedTemplate_deletesFromRepository() {
        when(templateRepository.findById(7L)).thenReturn(Optional.of(template));

        templateService.delete("bob", 7L);

        verify(templateRepository).delete(template);
    }

    @Test
    void delete_callerIsNotCreator_throwsAndDoesNotDelete() {
        when(templateRepository.findById(7L)).thenReturn(Optional.of(template));

        assertThatThrownBy(() -> templateService.delete("alice", 7L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Access denied");

        verify(templateRepository, never()).delete(any());
    }
}
