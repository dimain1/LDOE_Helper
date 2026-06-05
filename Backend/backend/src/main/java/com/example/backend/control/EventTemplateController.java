package com.example.backend.control;

import com.example.backend.dto.template.EventTemplateCreateRequest;
import com.example.backend.dto.template.EventTemplateDto;
import com.example.backend.dto.template.EventTemplateUpdateRequest;
import com.example.backend.mediator.EventTemplateService;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/templates")
public class EventTemplateController {

    private final EventTemplateService templateService;

    public EventTemplateController(EventTemplateService templateService) {
        this.templateService = templateService;
    }

    /** Все шаблоны текущего пользователя. */
    @GetMapping
    public List<EventTemplateDto> getMyTemplates(
            @AuthenticationPrincipal UserDetails userDetails) {
        return templateService.getMyTemplates(userDetails.getUsername());
    }

    /**
     * Создать шаблон.
     * Multipart: поле "request" (JSON) + необязательное поле "image".
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public EventTemplateDto create(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestPart EventTemplateCreateRequest request,
            @RequestPart(required = false) MultipartFile image) {
        return templateService.create(userDetails.getUsername(), request, image);
    }

    /**
     * Обновить шаблон.
     * Multipart: поле "request" (JSON) + необязательное поле "image".
     */
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public EventTemplateDto update(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @RequestPart EventTemplateUpdateRequest request,
            @RequestPart(required = false) MultipartFile image) {
        return templateService.update(userDetails.getUsername(), id, request, image);
    }

    /** Удалить шаблон (только свой). */
    @DeleteMapping("/{id}")
    public void delete(@AuthenticationPrincipal UserDetails userDetails,
                       @PathVariable Long id) {
        templateService.delete(userDetails.getUsername(), id);
    }
}
