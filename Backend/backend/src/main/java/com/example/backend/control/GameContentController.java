package com.example.backend.control;

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

    /** Весь контент с флагом pinned для текущего пользователя. */
    @GetMapping
    public List<GameContentDto> getAll(@AuthenticationPrincipal UserDetails userDetails) {
        return gameContentService.getAll(userDetails.getUsername());
    }

    /** Создать контент (только ADMIN). Multipart: "request" + "file". */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public GameContentDto create(
            @RequestPart GameContentRequest request,
            @RequestPart(required = false) MultipartFile file) {
        return gameContentService.create(request, file);
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
