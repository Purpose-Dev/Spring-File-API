package com.purpose.file_api.service;

import com.purpose.file_api.exception.FileNotFoundException;
import com.purpose.file_api.exception.FileStorageException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
@Slf4j
public class FileService {
    private static final long MAX_FILE_SIZE_MB = 10;
    private static final long MAX_FILE_SIZE_BYTES = MAX_FILE_SIZE_MB * 1024 * 1024;


    @Value("${file.upload.directory}")
    private String uploadDirectory;
    private Path uploadPath;
    private final Tika tika = new Tika();

    public FileService() {}

    private Path getUploadPath() {
        if (uploadPath == null) {
            uploadPath = Paths.get(uploadDirectory)
                    .toAbsolutePath()
                    .normalize();
            log.debug("Upload path resolved to: {}", uploadPath);
        }

        return uploadPath;
    }

    @PostConstruct
    public void createDirIfNotExist() {
        try {
            if (Files.notExists(getUploadPath())) {
                Files.createDirectories(getUploadPath());
                log.info("Upload directory created: {}", uploadDirectory);
            } else {
                log.info("Upload directory already exists: {}", uploadDirectory);
            }
        } catch (Exception e) {
            log.error("Could not create directory: {}", uploadDirectory, e);
            throw new FileStorageException("Could not create directory: " + uploadDirectory);
        }
    }

    public String save(@NonNull MultipartFile file) {
        String originalFilename = file.getOriginalFilename();

        if (originalFilename == null) {
            log.error("Failed to get original filename for file upload.");
            throw new FileStorageException("Could not upload file: No filename provided.");
        }

        try {
            validateFileSize(file);
            String detectedType = tika.detect(file.getInputStream());
            log.info("Detected file type for '{}': {}", originalFilename, detectedType);

            Path uploadFile = getUploadPath().resolve(originalFilename);
            Files.copy(file.getInputStream(), uploadFile, StandardCopyOption.REPLACE_EXISTING);
            log.info("File uploaded successfully: {}", originalFilename);

            return uploadFile.getFileName().toString();
        } catch (Exception e) {
            log.error("Could not upload file '{}': {}", originalFilename, e.getMessage(), e);
            throw new FileStorageException("Could not upload file: " + originalFilename);
        }
    }

    public Resource load(String fileName) {
        try {
            Path file = getUploadPath().resolve(fileName).normalize();
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() && resource.isReadable()) {
                log.info("File loaded successfully: {}", fileName);
                return resource;
            } else {
                log.warn("File not found or not readable: {}", fileName);
                throw new FileNotFoundException("File not found: " + fileName);
            }
        } catch (Exception e) {
            log.error("File cannot be downloaded '{}': {}", fileName, e.getMessage(), e);
            throw new FileNotFoundException("File cannot be downloaded: " + fileName);
        }
    }

    private void validateFileSize(MultipartFile file) {
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            log.warn("File size exceeds the maximum allowed size of 10MB: {} bytes", file.getSize());
            throw new FileStorageException("File size exceeds the maximum allowed size of 10MB");
        }
    }
}
