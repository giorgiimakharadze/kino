package com.kinoapp.kino.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");

    private final Path uploadDir;

    // Folder comes from application.properties
    public FileStorageService(@Value("${app.upload-dir}") String dir) {
        this.uploadDir = Path.of(dir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(uploadDir);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot create upload folder", e);
        }
    }

    // Saves the image under a random name and returns that name for the database
    public String store(MultipartFile file) {
        String ext = StringUtils.getFilenameExtension(file.getOriginalFilename());
        if (ext == null || !ALLOWED_EXTENSIONS.contains(ext.toLowerCase())) {
            throw new IllegalArgumentException("Poster must be a jpg, png or webp image");
        }
        // Random name for the file
        String name = UUID.randomUUID() + "." + ext.toLowerCase();
        try {
            // Recreate the folder in case it was deleted while the app runs
            Files.createDirectories(uploadDir);
            Files.copy(file.getInputStream(), uploadDir.resolve(name), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not save poster", e);
        }
        return name;
    }

    public void delete(String name) {
        // Seeded movies keep a poster web address, not a file on disk
        if (name == null || name.startsWith("http")) return;
        try {
            Files.deleteIfExists(uploadDir.resolve(name));
        } catch (IOException e) {
            throw new UncheckedIOException("Could not delete poster", e);
        }
    }

    public Path getUploadDir() {
        return uploadDir;
    }
}
