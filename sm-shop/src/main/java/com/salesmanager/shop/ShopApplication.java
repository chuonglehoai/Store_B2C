package com.salesmanager.shop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.salesmanager")
@EntityScan(basePackages = "com.salesmanager.core.model")
@EnableJpaRepositories(basePackages = "com.salesmanager.core.business.repositories")
public class ShopApplication { 

    public static void main(String[] args) {
        SpringApplication.run(ShopApplication.class, args);
    }
}