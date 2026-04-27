package com.example.backend.control;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.multipart.MultipartFile;

import com.example.backend.mediator.GameContentService;
import com.example.backend.dto.gameContent.GameContentRequest;
import com.example.backend.entity.GameContent;

import jakarta.transaction.Transactional;


@RestController
@RequestMapping("/content")
public class GameContentController {

    GameContentService gameContentService;

    public GameContentController(GameContentService gameContentService) {
        this.gameContentService = gameContentService;
    }

    @PostMapping(value="/create", consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public GameContent createContent(
        @RequestPart GameContentRequest request,
        @RequestPart MultipartFile file
    ) {
        return gameContentService.create(request, file);
    }

}
