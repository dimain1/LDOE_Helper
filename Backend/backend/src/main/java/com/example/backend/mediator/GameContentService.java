package com.example.backend.mediator;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.example.backend.dto.gameContent.GameContentRequest;
import com.example.backend.entity.ContentType;
import com.example.backend.entity.GameContent;
import java.util.Set;
import java.util.stream.Collectors;
import com.example.backend.foundation.repository.ContentTypeRepository;
import com.example.backend.foundation.repository.GameContentRepository;

@Service
public class GameContentService {

    private final ImageStorageService imageStorageService;
    private final GameContentRepository gameContentRepository;
    private final ContentTypeRepository contentTypeRepository;

    public GameContentService(ImageStorageService imageStorageService, GameContentRepository gameContentRepository, ContentTypeRepository contentTypeRepository) {
        this.imageStorageService = imageStorageService;
        this.gameContentRepository = gameContentRepository;
        this.contentTypeRepository = contentTypeRepository;
    }

    @Transactional
    public GameContent create(GameContentRequest request, MultipartFile file) {
        
        String imagePath = imageStorageService.saveImage(file);

        GameContent content = new GameContent();
        content.setName(request.getName());
        content.setDescription(request.getDescription());
        content.setImageUrl(imagePath);
        Set<ContentType> types = request.getTypeIds().stream()
            .map(id -> contentTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Content type not found: " + id)))
            .collect(Collectors.toSet());


        content.setTypes(types);
        content.setProperties(request.getProperties());

        return gameContentRepository.save(content);

    }

}
