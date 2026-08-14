package com.atlaspay.app.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class SimulatorConfig {

    @Value("${atlaspay.simulator.base-url:http://localhost:8080}")
    private String simulatorBaseUrl;

    @Bean(name = "simulatorRestClient")
    public RestClient simulatorRestClient(RestClient.Builder builder) {
        return builder
                .baseUrl(simulatorBaseUrl)
                .build();
    }
}
