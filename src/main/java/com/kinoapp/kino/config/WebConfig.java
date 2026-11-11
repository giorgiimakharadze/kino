package com.kinoapp.kino.config;

import com.kinoapp.kino.service.FileStorageService;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final FileStorageService fileStorage;

    public WebConfig(FileStorageService fileStorage) {
        this.fileStorage = fileStorage;
    }

    // Serve uploaded posters from disk
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/posters/**")
                .addResourceLocations(fileStorage.getUploadDir().toUri().toString());
    }
}
