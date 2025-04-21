package com.java.kokodi.service;

import com.java.kokodi.dto.UserDto;
import com.java.kokodi.entity.GameSession;
import com.java.kokodi.entity.User;
import com.java.kokodi.enums.Role;
import com.java.kokodi.exception.EmailAlreadyExistsException;
import com.java.kokodi.exception.InvalidPasswordException;
import com.java.kokodi.exception.UserException;
import com.java.kokodi.mapper.UserMapper;
import com.java.kokodi.repository.GameSessionRepository;
import com.java.kokodi.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final GameSessionRepository gameSessionRepository;

    public UserService(UserRepository userRepository, UserMapper userMapper, PasswordEncoder passwordEncoder, GameSessionRepository gameSessionRepository) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.gameSessionRepository = gameSessionRepository;
    }

    @Transactional(readOnly = true)
    public UserDto getUserById(UUID id){
        return userMapper.toDto(
                userRepository.findById(id)
                        .orElseThrow(()-> new UserException("User not found"))
        );
    }

    @Transactional(readOnly = true)
    public User getEntityById(UUID id){
        return  userRepository.findById(id)
                .orElseThrow(()-> new UserException("User not found"));
    }

    public User registerUser(String login, String name, String email, String password, Set<Role> roles) { // Добавлены login и name
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException("Email already exists: " + email);
        }
        if (userRepository.existsByLogin(login)) {
            throw new UserException("Login already taken: " + login);
        }

        User user = new User();
        user.setLogin(login);
        user.setName(name);  // Устанавливаем обязательные поля
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRoles(roles);

        return userRepository.save(user);
    }

    /**
     * Создает пользователя с ролью администратора.
     *
     * @param email    электронная почта
     * @param password пароль
     * @return пользователь с ролью ROLE_ADMIN
     */
    public User createAdmin(String login, String name, String email, String password) {
        return registerUser(login, name, email, password, Set.of(Role.ADMIN)); // Исправлено
    }
    /**
     * Создает обычного пользователя.
     *
     * @param email    электронная почта
     * @param password пароль
     * @return пользователь с ролью ROLE_USER
     */
    public User createUser(String login, String name, String email, String password) {
        return registerUser(login, name, email, password, Set.of(Role.USER)); // Исправлено
    }

    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers(){
        return userMapper.toDtoList(userRepository.findAll());
    }

    @Transactional
    public void deleteUser(UUID id){
        userRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public boolean existsByLogin(String login){
        return userRepository.existsByLogin(login);
    }
    public int getUserScore(User user, GameSession gameSession) {
        return gameSession.getPlayerScores().getOrDefault(user.getId(), 0);
    }

    @Transactional
    public int addScore(User user, GameSession gameSession, int points) {
        Map<UUID, Integer> scores = gameSession.getPlayerScores();
        int newScore = Math.max(0, scores.getOrDefault(user.getId(), 0) + points);
        scores.put(user.getId(), newScore);
        gameSessionRepository.save(gameSession);
        return newScore;
    }
    public Optional<User> findByLogin(String login) {
        return userRepository.findByLogin(login);
    }


}