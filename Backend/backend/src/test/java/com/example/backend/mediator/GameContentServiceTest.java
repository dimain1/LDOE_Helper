package com.example.backend.mediator;

import com.example.backend.dto.gameContent.ContentTypeDto;
import com.example.backend.dto.gameContent.GameContentDto;
import com.example.backend.dto.gameContent.GameContentRequest;
import com.example.backend.entity.ContentType;
import com.example.backend.entity.GameContent;
import com.example.backend.entity.User;
import com.example.backend.entity.UserPinnedContent;
import com.example.backend.entity.UserRole;
import com.example.backend.foundation.repository.ContentTypeRepository;
import com.example.backend.foundation.repository.GameContentRepository;
import com.example.backend.foundation.repository.UserPinnedContentRepository;
import com.example.backend.foundation.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameContentServiceTest {

    @Mock
    private ImageStorageService imageStorageService;

    @Mock
    private GameContentRepository gameContentRepository;

    @Mock
    private ContentTypeRepository contentTypeRepository;

    @Mock
    private UserPinnedContentRepository pinnedRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private GameContentService gameContentService;

    private User user;
    private ContentType allType;
    private GameContent content;

    @BeforeEach
    void setUp() {
        UserRole role = new UserRole();
        role.setName("USER");

        user = new User("carol", "hashed", "carol@mail.com", role);
        user.setId(3L);

        allType = new ContentType();
        allType.setId(1L);
        allType.setName("All");

        content = new GameContent();
        content.setId(100L);
        content.setName("AK-74");
        content.setTypes(new HashSet<>(Set.of(allType)));
    }

    // ── ensureAllTypeExists ────────────────────────────────────────────────────

    @Test
    void ensureAllTypeExists_typeAlreadyPresent_doesNotCreateNew() {
        when(contentTypeRepository.findByName("All")).thenReturn(Optional.of(allType));

        gameContentService.ensureAllTypeExists();

        verify(contentTypeRepository, never()).save(any());
    }

    @Test
    void ensureAllTypeExists_typeMissing_createsAndSaves() {
        when(contentTypeRepository.findByName("All")).thenReturn(Optional.empty());
        when(contentTypeRepository.save(any())).thenReturn(allType);

        gameContentService.ensureAllTypeExists();

        verify(contentTypeRepository).save(argThat(t -> "All".equals(t.getName())));
    }

    // ── getAllTypes ────────────────────────────────────────────────────────────

    @Test
    void getAllTypes_returnsDtoForEachType() {
        ContentType weapons = new ContentType();
        weapons.setId(2L);
        weapons.setName("Weapons");

        when(contentTypeRepository.findAll()).thenReturn(List.of(allType, weapons));

        List<ContentTypeDto> result = gameContentService.getAllTypes();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(ContentTypeDto::getName)
                .containsExactlyInAnyOrder("All", "Weapons");
    }

    // ── createType ────────────────────────────────────────────────────────────

    @Test
    void createType_newName_savesAndReturnsDto() {
        ContentType saved = new ContentType();
        saved.setId(2L);
        saved.setName("Weapons");

        when(contentTypeRepository.findByName("Weapons")).thenReturn(Optional.empty());
        when(contentTypeRepository.save(any())).thenReturn(saved);

        ContentTypeDto dto = gameContentService.createType("Weapons");

        assertThat(dto.getName()).isEqualTo("Weapons");
        assertThat(dto.getId()).isEqualTo(2L);
    }

    @Test
    void createType_duplicateName_throwsRuntimeException() {
        when(contentTypeRepository.findByName("Weapons")).thenReturn(Optional.of(new ContentType()));

        assertThatThrownBy(() -> gameContentService.createType("Weapons"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("уже существует");
    }

    // ── deleteType ────────────────────────────────────────────────────────────

    @Test
    void deleteType_typeNotFound_throws() {
        when(contentTypeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gameContentService.deleteType(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Тип не найден");
    }

    @Test
    void deleteType_allType_throwsAndDoesNotDelete() {
        when(contentTypeRepository.findById(1L)).thenReturn(Optional.of(allType));

        assertThatThrownBy(() -> gameContentService.deleteType(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("нельзя удалить");

        verify(contentTypeRepository, never()).deleteById(any());
    }

    @Test
    void deleteType_regularType_callsDeleteById() {
        ContentType weapons = new ContentType();
        weapons.setId(2L);
        weapons.setName("Weapons");

        when(contentTypeRepository.findById(2L)).thenReturn(Optional.of(weapons));

        gameContentService.deleteType(2L);

        verify(contentTypeRepository).deleteById(2L);
    }

    // ── getAll ────────────────────────────────────────────────────────────────

    @Test
    void getAll_returnsDtosWithCorrectPinnedFlag() {
        List<Object[]> rows = new ArrayList<>();
        rows.add(new Object[]{content, Boolean.TRUE});

        when(userRepository.findByLogin("carol")).thenReturn(Optional.of(user));
        when(gameContentRepository.findAllWithPinnedFlag(3L)).thenReturn(rows);

        List<GameContentDto> result = gameContentService.getAll("carol");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("AK-74");
        assertThat(result.get(0).isPinned()).isTrue();
    }

    @Test
    void getAll_unknownLogin_throws() {
        when(userRepository.findByLogin("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gameContentService.getAll("ghost"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found");
    }

    // ── create ────────────────────────────────────────────────────────────────

    @Test
    void create_withoutFile_savesContentWithAllType() {
        GameContent savedContent = new GameContent();
        savedContent.setId(100L);
        savedContent.setName("AK-74");

        when(contentTypeRepository.findByName("All")).thenReturn(Optional.of(allType));
        when(gameContentRepository.save(any())).thenReturn(savedContent);

        GameContentRequest req = new GameContentRequest();
        req.setName("AK-74");

        GameContentDto dto = gameContentService.create(req, null);

        assertThat(dto.getName()).isEqualTo("AK-74");
        verify(imageStorageService, never()).saveImage(any());
        verify(gameContentRepository).save(any(GameContent.class));
    }

    // ── pin / unpin ───────────────────────────────────────────────────────────

    @Test
    void pin_contentNotYetPinned_savesNewPin() {
        when(userRepository.findByLogin("carol")).thenReturn(Optional.of(user));
        when(gameContentRepository.findById(100L)).thenReturn(Optional.of(content));
        when(pinnedRepository.existsByUserAndContent(user, content)).thenReturn(false);

        gameContentService.pin("carol", 100L);

        verify(pinnedRepository).save(any(UserPinnedContent.class));
    }

    @Test
    void pin_alreadyPinned_doesNotSaveAgain() {
        when(userRepository.findByLogin("carol")).thenReturn(Optional.of(user));
        when(gameContentRepository.findById(100L)).thenReturn(Optional.of(content));
        when(pinnedRepository.existsByUserAndContent(user, content)).thenReturn(true);

        gameContentService.pin("carol", 100L);

        verify(pinnedRepository, never()).save(any());
    }

    @Test
    void pin_contentNotFound_throws() {
        when(userRepository.findByLogin("carol")).thenReturn(Optional.of(user));
        when(gameContentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gameContentService.pin("carol", 999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Content not found");
    }

    @Test
    void unpin_callsDeleteByUserAndContent() {
        when(userRepository.findByLogin("carol")).thenReturn(Optional.of(user));
        when(gameContentRepository.findById(100L)).thenReturn(Optional.of(content));

        gameContentService.unpin("carol", 100L);

        verify(pinnedRepository).deleteByUserAndContent(user, content);
    }

    @Test
    void unpin_contentNotFound_throws() {
        when(userRepository.findByLogin("carol")).thenReturn(Optional.of(user));
        when(gameContentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gameContentService.unpin("carol", 999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Content not found");
    }
}
