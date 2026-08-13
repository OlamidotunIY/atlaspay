package com.atlaspay.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * AtlasPay composition root.
 *
 * <p>The {@code @SpringBootApplication} scan is intentionally scoped to {@code com.atlaspay}
 * to pick up all module components (use cases, adapters, controllers) registered as Spring beans.
 */
@SpringBootApplication(scanBasePackages = "com.atlaspay")
@EntityScan(basePackages = "com.atlaspay")
@EnableJpaRepositories(basePackages = "com.atlaspay")
public class AtlasPayApplication {

    public static void main(String[] args) {
        SpringApplication.run(AtlasPayApplication.class, args);
    }
}
