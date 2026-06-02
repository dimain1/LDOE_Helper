package com.example.backend.mediator;

import com.example.backend.dto.gameContent.ContentTypeDto;
import com.example.backend.dto.gameContent.GameContentDto;
import com.example.backend.dto.gameContent.GameContentRequest;
import com.example.backend.entity.ContentType;
import com.example.backend.entity.GameContent;
import com.example.backend.entity.User;
import com.example.backend.entity.UserPinnedContent;
import com.example.backend.foundation.repository.ContentTypeRepository;
import com.example.backend.foundation.repository.GameContentRepository;
import com.example.backend.foundation.repository.UserPinnedContentRepository;
import com.example.backend.foundation.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class GameContentService {

    private final ImageStorageService imageStorageService;
    private final GameContentRepository gameContentRepository;
    private final ContentTypeRepository contentTypeRepository;
    private final UserPinnedContentRepository pinnedRepository;
    private final UserRepository userRepository;

    public GameContentService(ImageStorageService imageStorageService,
                              GameContentRepository gameContentRepository,
                              ContentTypeRepository contentTypeRepository,
                              UserPinnedContentRepository pinnedRepository,
                              UserRepository userRepository) {
        this.imageStorageService = imageStorageService;
        this.gameContentRepository = gameContentRepository;
        this.contentTypeRepository = contentTypeRepository;
        this.pinnedRepository = pinnedRepository;
        this.userRepository = userRepository;
    }

    /** Гарантирует существование типа "All" при старте сервера. */
    @PostConstruct
    @Transactional
    public void ensureAllTypeExists() {
        contentTypeRepository.findByName("All").orElseGet(() -> {
            ContentType allType = new ContentType();
            allType.setName("All");
            return contentTypeRepository.save(allType);
        });
    }

    /** Все типы контента. */
    public List<ContentTypeDto> getAllTypes() {
        return contentTypeRepository.findAll().stream()
                .map(t -> new ContentTypeDto(t.getId(), t.getName()))
                .collect(Collectors.toList());
    }

    /** Создать новый тип контента. */
    @Transactional
    public ContentTypeDto createType(String name) {
        if (contentTypeRepository.findByName(name).isPresent()) {
            throw new RuntimeException("Тип с именем '" + name + "' уже существует");
        }
        ContentType type = new ContentType();
        type.setName(name);
        ContentType saved = contentTypeRepository.save(type);
        return new ContentTypeDto(saved.getId(), saved.getName());
    }

    /** Удалить тип контента. Тип "All" защищён от удаления. */
    @Transactional
    public void deleteType(Long id) {
        ContentType type = contentTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Тип не найден: " + id));
        if ("All".equals(type.getName())) {
            throw new RuntimeException("Тип 'All' нельзя удалить");
        }
        contentTypeRepository.deleteById(id);
    }

    /** Весь контент с флагом pinned для текущего пользователя. */
    public List<GameContentDto> getAll(String login) {
        User user = findUser(login);
        return gameContentRepository.findAllWithPinnedFlag(user.getId())
                .stream()
                .map(row -> toDto((GameContent) row[0], (Boolean) row[1]))
                .toList();
    }

    @Transactional
    public GameContentDto create(GameContentRequest request, MultipartFile file) {
        String imagePath = (file != null && !file.isEmpty())
                ? imageStorageService.saveImage(file) : null;

        GameContent content = new GameContent();
        content.setName(request.getName());
        content.setDescription(request.getDescription());
        content.setImageUrl(imagePath);
        content.setAttributes(request.getAttributes());

        // Группа "All" всегда присутствует — находим или создаём
        ContentType allType = contentTypeRepository.findByName("All")
                .orElseGet(() -> {
                    ContentType newAll = new ContentType();
                    newAll.setName("All");
                    return contentTypeRepository.save(newAll);
                });

        Set<ContentType> types = new java.util.HashSet<>();
        types.add(allType);

        if (request.getTypeIds() != null) {
            request.getTypeIds().stream()
                    .map(id -> contentTypeRepository.findById(id)
                            .orElseThrow(() -> new RuntimeException("Type not found: " + id)))
                    .forEach(types::add);
        }
        content.setTypes(types);

        return toDto(gameContentRepository.save(content), false);
    }

    @Transactional
    public void pin(String login, Long contentId) {
        User user = findUser(login);
        GameContent content = gameContentRepository.findById(contentId)
                .orElseThrow(() -> new RuntimeException("Content not found: " + contentId));

        if (!pinnedRepository.existsByUserAndContent(user, content)) {
            pinnedRepository.save(new UserPinnedContent(user, content));
        }
    }

    @Transactional
    public void unpin(String login, Long contentId) {
        User user = findUser(login);
        GameContent content = gameContentRepository.findById(contentId)
                .orElseThrow(() -> new RuntimeException("Content not found: " + contentId));
        pinnedRepository.deleteByUserAndContent(user, content);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private User findUser(String login) {
        return userRepository.findByLogin(login)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public static GameContentDto toDto(GameContent gc, boolean pinned) {
        GameContentDto dto = new GameContentDto();
        dto.setId(gc.getId());
        dto.setName(gc.getName());
        dto.setDescription(gc.getDescription());
        dto.setImageUrl(gc.getImageUrl());
        dto.setAttributes(gc.getAttributes());
        dto.setPinned(pinned);

        Set<ContentTypeDto> typeDtos = gc.getTypes().stream()
                .map(t -> new ContentTypeDto(t.getId(), t.getName()))
                .collect(Collectors.toSet());
        dto.setTypes(typeDtos);

        return dto;
    }
}
