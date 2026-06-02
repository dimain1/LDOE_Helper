package com.example.backend.mediator;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class ImageStorageService {

    private static final String STORAGE_DIR = "uploads/";

    /**
     * Сохраняет файл в uploads/ и возвращает URL-путь вида /images/uuid_filename.jpg
     * Этот путь сервируется через WebConfig без авторизации.
     */
    public String saveImage(MultipartFile file) {
        try {
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path path = Paths.get(STORAGE_DIR + fileName);
            Files.createDirectories(path.getParent());
            Files.write(path, file.getBytes());

            // Возвращаем URL-путь, а не путь ОС
            return "/images/" + fileName;

        } catch (IOException e) {
            throw new RuntimeException("Failed to save image: " + e.getMessage(), e);
        }
    }
}
