package com.java.kokodi.service;

import com.java.kokodi.dto.UserDto;
import com.java.kokodi.entity.GameSession;
import com.java.kokodi.entity.User;
import com.java.kokodi.enums.Role;
import com.java.kokodi.exception.EmailAlreadyExistsException;
import com.java.kokodi.exception.UserException;
import com.java.kokodi.mapper.UserMapper;
import com.java.kokodi.repository.GameSessionRepository;
import com.java.kokodi.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Сервис для работы с пользователями.
 * Обеспечивает регистрацию, аутентификацию и управление пользователями.
 */
@Service
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final GameSessionRepository gameSessionRepository;

    public UserService(UserRepository userRepository, UserMapper userMapper,
                       PasswordEncoder passwordEncoder, GameSessionRepository gameSessionRepository) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.gameSessionRepository = gameSessionRepository;
    }

    /**
     * Получает пользователя по идентификатору.
     *
     * @param id UUID пользователя
     * @return DTO пользователя
     * @throws UserException если пользователь не найден
     */
    @Transactional(readOnly = true)
    public UserDto getUserById(UUID id) {
        return userMapper.toDto(
                userRepository.findById(id)
                        .orElseThrow(() -> new UserException("User not found"))
        );
    }

    /**
     * Получает сущность пользователя по идентификатору.
     *
     * @param id UUID пользователя
     * @return сущность пользователя
     * @throws UserException если пользователь не найден
     */
    @Transactional(readOnly = true)
    public User getEntityById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserException("User not found"));
    }

    /**
     * Регистрирует нового пользователя.
     *
     * @param login логин пользователя
     * @param name имя пользователя
     * @param email электронная почта
     * @param password пароль
     * @param roles набор ролей пользователя
     * @return зарегистрированный пользователь
     * @throws EmailAlreadyExistsException если email уже занят
     * @throws UserException если логин уже занят
     */
    public User registerUser(String login, String name, String email, String password, Set<Role> roles) {
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException("Email already exists: " + email);
        }
        if (userRepository.existsByLogin(login)) {
            throw new UserException("Login already taken: " + login);
        }

        User user = new User();
        user.setLogin(login);
        user.setName(name);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRoles(roles);

        return userRepository.save(user);
    }

    /**
     * Создает пользователя с ролью администратора.
     *
     * @param login логин администратора
     * @param name имя администратора
     * @param email электронная почта
     * @param password пароль
     * @return пользователь с ролью ROLE_ADMIN
     */
    public User createAdmin(String login, String name, String email, String password) {
        return registerUser(login, name, email, password, Set.of(Role.ADMIN));
    }

    /**
     * Создает обычного пользователя.
     *
     * @param login логин пользователя
     * @param name имя пользователя
     * @param email электронная почта
     * @param password пароль
     * @return пользователь с ролью ROLE_USER
     */
    public User createUser(String login, String name, String email, String password) {
        return registerUser(login, name, email, password, Set.of(Role.USER));
    }

    /**
     * Получает список всех пользователей.
     *
     * @return список DTO всех пользователей
     */
    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers() {
        return userMapper.toDtoList(userRepository.findAll());
    }
    @Transactional
    public int getUserScore(User user, GameSession session) {
        return session.getPlayerScores()
                .getOrDefault(user.getId(), 0);
    }

    /**
     * Изменяет счет пользователя в игровой сессии.
     *
     * @param user пользователь
     * @param session игровая сессия
     * @param delta изменение счета
     * @return новый счет пользователя
     */
    @Transactional
    public int addScore(User user, GameSession session, int delta) {
        Map<UUID, Integer> scores = new HashMap<>(session.getPlayerScores());
        int current = scores.getOrDefault(user.getId(), 0);
        int newScore = Math.max(0, current + delta);
        scores.put(user.getId(), newScore);
        session.setPlayerScores(scores);
        gameSessionRepository.save(session);
        return newScore;
    }
}