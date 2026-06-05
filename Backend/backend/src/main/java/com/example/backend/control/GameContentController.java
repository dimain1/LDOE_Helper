package com.example.backend.control;

import com.example.backend.dto.gameContent.ContentTypeDto;
import com.example.backend.dto.gameContent.GameContentDto;
import com.example.backend.dto.gameContent.GameContentRequest;
import com.example.backend.mediator.GameContentService;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/content")
public class GameContentController {

    private final GameContentService gameContentService;

    public GameContentController(GameContentService gameContentService) {
        this.gameContentService = gameContentService;
    }

    /** Весь контент с флагом pinned для текущего пользователя.
     *  Для анонимных запросов (userDetails == null) возвращает контент с pinned = false. */
    @GetMapping
    public List<GameContentDto> getAll(@AuthenticationPrincipal UserDetails userDetails) {
        String login = (userDetails != null) ? userDetails.getUsername() : null;
        return gameContentService.getAll(login);
    }

    /** Создать контент (только ADMIN). Multipart: "request" + "file". */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public GameContentDto create(
            @RequestPart GameContentRequest request,
            @RequestPart(required = false) MultipartFile file) {
        return gameContentService.create(request, file);
    }

    /** Все доступные типы контента. */
    @GetMapping("/types")
    public List<ContentTypeDto> getAllTypes() {
        return gameContentService.getAllTypes();
    }

    /** Создать тип контента (только ADMIN). */
    @PostMapping("/types")
    @PreAuthorize("hasRole('ADMIN')")
    public ContentTypeDto createType(@RequestBody ContentTypeDto request) {
        return gameContentService.createType(request.getName());
    }

    /** Удалить тип контента (только ADMIN). Нельзя удалить "All". */
    @DeleteMapping("/types/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteType(@PathVariable Long id) {
        gameContentService.deleteType(id);
    }

    /** Закрепить контент для текущего пользователя. */
    @PostMapping("/{id}/pin")
    public void pin(@AuthenticationPrincipal UserDetails userDetails,
                    @PathVariable Long id) {
        gameContentService.pin(userDetails.getUsername(), id);
    }

    /** Открепить контент. */
    @DeleteMapping("/{id}/pin")
    public void unpin(@AuthenticationPrincipal UserDetails userDetails,
                      @PathVariable Long id) {
        gameContentService.unpin(userDetails.getUsername(), id);
    }
}
