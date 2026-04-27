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

    private final String storagePath = "uploads/";

    public String saveImage(MultipartFile file){

        try{
            String filePath = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();

            Path path = Paths.get(storagePath + filePath);
            Files.createDirectories(path.getParent());
            Files.write(path, file.getBytes());

            return path.toString();
        }

        catch (IOException e){
            throw new RuntimeException("Failed to save image", e);
        }

    }

}
