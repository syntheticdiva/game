package com.java.kokodi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO для запроса аутентификации
 */
@Data
@Schema(description = "Запрос на аутентификацию пользователя")
public class AuthRequest {

    @Schema(
            description = "Электронная почта пользователя",
            example = "user@example.com",
            requiredMode = Schema.RequiredMode.REQUIRED,
            maxLength = 100
    )
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Size(max = 100, message = "Email must be less than 100 characters")
    private String email;

    @Schema(
            description = "Пароль пользователя",
            example = "P@ssw0rd!",
            requiredMode = Schema.RequiredMode.REQUIRED,
            minLength = 8,
            maxLength = 100
    )
    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be 8-100 characters")
    private String password;
}