package com.java.kokodi.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    /**
     * Создает базовую конфигурацию OpenAPI с метаданными и аутентификацией.
     *
     * @return настроенный объект OpenAPI со следующими параметрами:
     * <ul>
     *   <li>Название API: "Task Management API"</li>
     *   <li>Версия API: "1.0"</li>
     *   <li>Тип аутентификации: Bearer JWT</li>
     *   <li>Описание: "API Documentation for Task Management System"</li>
     * </ul>
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                        )
                )
                .info(new Info()
                        .title("Task Management API")
                        .version("1.0")
                        .description("API Documentation for Task Management System")
                );
    }

    /**
     * Группирует все публичные API-эндпоинты.
     *
     * @return сконфигурированная группа API со следующими параметрами:
     * <ul>
     *   <li>Название группы: "public-apis"</li>
     *   <li>Паттерн путей: все эндпоинты (/**)</li>
     * </ul>
     */
    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("public-apis")
                .pathsToMatch("/**")
                .build();
    }
}
