package com.java.kokodi.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Фильтр для обработки JWT-токенов во входящих запросах.
 * <p>
 * Наследует {@link OncePerRequestFilter} для однократной обработки каждого запроса.
 * Выполняет следующие задачи:
 * <ol>
 *   <li>Извлекает JWT из заголовка Authorization</li>
 *   <li>Проверяет валидность токена (срок действия, подпись)</li>
 *   <li>Загружает данные пользователя при успешной проверке</li>
 *   <li>Устанавливает аутентификацию в контекст безопасности</li>
 * </ol>
 */
@Component
@RequiredArgsConstructor
public class JwtRequestFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    /**
     * Основной метод обработки запроса.
     *
     * @param request HTTP-запрос
     * @param response HTTP-ответ
     * @param chain цепочка фильтров
     * @throws ServletException при ошибках сервлета
     * @throws IOException при ошибках ввода/вывода
     *
     * @implSpec Логика работы:
     * 1. Проверка заголовка Authorization
     * 2. Извлечение и валидация JWT
     * 3. Обработка исключений:
     *    - 401: Истекший токен
     *    - 403: Неверная подпись
     *    - 400: Прочие ошибки валидации
     * 4. Установка аутентификации в SecurityContext
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        String username = null;
        String jwt = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwt = authHeader.substring(7);
            try {
                username = jwtUtil.extractUsername(jwt);
            } catch (ExpiredJwtException ex) {
                logger.warn("JWT token expired: " + ex.getMessage());
                response.sendError(401, "JWT expired");
                return;
            } catch (SignatureException ex) {
                logger.warn("Invalid JWT signature: " + ex.getMessage());
                response.sendError(403, "Invalid signature");
                return;
            } catch (Exception ex) {
                logger.warn("Invalid JWT token: " + ex.getMessage());
                response.sendError(400, "Invalid token");
                return;
            }
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            if (jwtUtil.validateToken(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        chain.doFilter(request, response);
    }
}