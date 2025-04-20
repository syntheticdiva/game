package com.java.kokodi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO для регистрации нового пользователя
 */
@Data
@Schema(description = "Запрос на регистрацию пользователя")
public class RegisterRequest {
    @NotBlank String login;
    @NotBlank String name;

    @Schema(
            description = "Электронная почта пользователя",
            example = "user@example.com",
            requiredMode = Schema.RequiredMode.REQUIRED,
            maxLength = 100
    )
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email must be less than 100 characters")
    private String email;

    @Schema(
            description = "Пароль (требования: 8-100 символов, минимум 1 заглавная буква, 1 строчная буква и 1 цифра)",
            example = "SecurePass123",
            requiredMode = Schema.RequiredMode.REQUIRED,
            minLength = 8,
            maxLength = 100
    )
    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be 8-100 characters")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
            message = "Password must contain at least one uppercase, one lowercase letter and one number"
    )
    private String password;
}
