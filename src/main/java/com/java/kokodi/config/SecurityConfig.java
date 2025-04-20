package com.java.kokodi.config;

import com.java.kokodi.security.JwtRequestFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
/**
 * Конфигурация безопасности приложения.
 * <p>
 * Настраивает:
 * <ul>
 *   <li>Правила доступа к эндпоинтам</li>
 *   <li>JWT-аутентификацию</li>
 *   <li>CORS политики</li>
 *   <li>Кодирование паролей</li>
 *   <li>Управление сессиями</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    /**
     * Внутренний класс с константами безопасности.
     * <p>
     * Содержит пути, доступные без аутентификации:
     * <ul>
     *   <li>Эндпоинты аутентификации</li>
     *   <li>Документация Swagger</li>
     *   <li>Статические ресурсы</li>
     * </ul>
     */
    private static final class SecurityConstants {
        private static final String[] PUBLIC_PATHS = {
                "/api/auth/**",
                "/swagger-ui/**",
                "/v3/api-docs/**",
                "/webjars/**",
                "/swagger-resources/**",
                "/swagger-ui.html"
        };
    }

    private final JwtRequestFilter jwtRequestFilter;

    public SecurityConfig(JwtRequestFilter jwtRequestFilter) {
        this.jwtRequestFilter = jwtRequestFilter;
    }

    /**
     * Конфигурирует цепочку фильтров безопасности.
     *
     * @param http объект конфигурации HTTP безопасности
     * @return сконфигурированная цепочка фильтров
     * @throws Exception при ошибках конфигурации
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(SecurityConstants.PUBLIC_PATHS).permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Конфигурирует политики CORS.
     * <p>
     * Разрешает:
     * <ul>
     *   <li>Все источники</li>
     *   <li>Все HTTP-методы</li>
     *   <li>Все заголовки</li>
     * </ul>
     *
     * @return источник конфигурации CORS
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOrigin("*");
        configuration.addAllowedMethod("*");
        configuration.addAllowedHeader("*");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * Создает кодировщик паролей BCrypt.
     *
     * @return реализация {@link PasswordEncoder} с алгоритмом BCrypt
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Предоставляет менеджер аутентификации Spring.
     *
     * @param authenticationConfiguration конфигурация аутентификации
     * @return менеджер аутентификации
     * @throws Exception при ошибках получения менеджера
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}