package com.salesmanager.shop.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebStaticConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Ánh xạ đường dẫn URL bắt đầu bằng /uploads/ vào thư mục uploads/ trên ổ cứng máy tính
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }
}