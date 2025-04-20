package com.java.kokodi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
/**
 * Ответ с JWT токеном аутентификации
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Ответ с JWT токеном после успешной аутентификации")
public class AuthResponse {

    @Schema(
            description = "JWT токен доступа",
            example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String token;
}
