package com.KR.FileExchanger.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/v1/files/**")
                .allowedOrigins("http://localhost:5500", "http://127.0.0.1:5500") // Разрешаем оба адреса
                .allowedMethods("GET", "POST", "DELETE")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}