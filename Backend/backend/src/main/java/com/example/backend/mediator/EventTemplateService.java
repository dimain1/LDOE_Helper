package com.example.backend.mediator;

import com.example.backend.dto.template.EventTemplateCreateRequest;
import com.example.backend.dto.template.EventTemplateDto;
import com.example.backend.dto.template.EventTemplateUpdateRequest;
import com.example.backend.entity.EventTemplate;
import com.example.backend.entity.User;
import com.example.backend.foundation.repository.EventTemplateRepository;
import com.example.backend.foundation.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class EventTemplateService {

    private final EventTemplateRepository templateRepository;
    private final UserRepository userRepository;
    private final ImageStorageService imageStorageService;

    public EventTemplateService(EventTemplateRepository templateRepository,
                                UserRepository userRepository,
                                ImageStorageService imageStorageService) {
        this.templateRepository = templateRepository;
        this.userRepository = userRepository;
        this.imageStorageService = imageStorageService;
    }

    /** Все шаблоны, созданные текущим пользователем. */
    public List<EventTemplateDto> getMyTemplates(String login) {
        User user = findUser(login);
        return templateRepository.findByCreator(user)
                .stream()
                .map(EventTemplateService::toDto)
                .toList();
    }

    @Transactional
    public EventTemplateDto create(String login,
                                   EventTemplateCreateRequest request,
                                   MultipartFile image) {
        User user = findUser(login);

        EventTemplate template = new EventTemplate();
        template.setCreator(user);
        template.setName(request.getName());
        template.setDescription(request.getDescription());
        template.setDuration(request.getDuration());

        if (image != null && !image.isEmpty()) {
            template.setImageUrl(imageStorageService.saveImage(image));
        }

        return toDto(templateRepository.save(template));
    }

    @Transactional
    public EventTemplateDto update(String login,
                                   Long templateId,
                                   EventTemplateUpdateRequest request,
                                   MultipartFile image) {
        EventTemplate template = findOwned(login, templateId);

        if (request.getName()        != null) template.setName(request.getName());
        if (request.getDescription() != null) template.setDescription(request.getDescription());
        if (request.getDuration()    != null) template.setDuration(request.getDuration());

        if (image != null && !image.isEmpty()) {
            template.setImageUrl(imageStorageService.saveImage(image));
        }

        return toDto(templateRepository.save(template));
    }

    @Transactional
    public void delete(String login, Long templateId) {
        EventTemplate template = findOwned(login, templateId);
        templateRepository.delete(template);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private User findUser(String login) {
        return userRepository.findByLogin(login)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private EventTemplate findOwned(String login, Long templateId) {
        EventTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new RuntimeException("Template not found: " + templateId));
        if (template.getCreator() == null || !template.getCreator().getLogin().equals(login)) {
            throw new RuntimeException("Access denied");
        }
        return template;
    }

    public static EventTemplateDto toDto(EventTemplate t) {
        EventTemplateDto dto = new EventTemplateDto();
        dto.setId(t.getId());
        dto.setCreatorId(t.getCreator() != null ? t.getCreator().getId() : null);
        dto.setName(t.getName());
        dto.setDescription(t.getDescription());
        dto.setImageUrl(t.getImageUrl());
        dto.setDuration(t.getDuration());
        return dto;
    }
}
