package com.salesmanager.shop.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                // Cho phép tất cả các nguồn (dùng allowedOriginPatterns thay vì allowedOrigins để tương thích với allowCredentials)
                .allowedOriginPatterns("*") 
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders("X-Auth-Token", "Content-Type", "Authorization", "Cache-Control", "X-Requested-With")
                .allowCredentials(true);
    }
}