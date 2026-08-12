package com.atlaspay.app.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI atlasPayOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("AtlasPay API")
                        .description("AtlasPay Core Banking and Payments API")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("AtlasPay Engineering")
                                .email("engineering@atlaspay.com"))
                        .license(new License().name("Proprietary").url("https://atlaspay.com/license")))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .addSecurityItem(new SecurityRequirement().addList("API Key Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication", createBearerScheme())
                        .addSecuritySchemes("API Key Authentication", createApiKeyScheme()));
    }

    private SecurityScheme createBearerScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .bearerFormat("JWT")
                .scheme("bearer")
                .description("Use a JWT for Merchant Dashboard authentication.");
    }

    private SecurityScheme createApiKeyScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.HEADER)
                .name("X-API-Key")
                .description("Use an API Key (Live or Test) for machine-to-machine API integration.");
    }
}
