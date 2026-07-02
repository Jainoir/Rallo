package com.rallo.notification.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI().info(new Info()
                .title("Rallo — Notification Service")
                .version("0.1.0")
                .description("Serves in-app notifications, consumes check-in events from RabbitMQ "
                        + "and runs the nightly streak reminder scheduler."));
    }
}
