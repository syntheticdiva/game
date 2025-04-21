package com.java.kokodi.service;

import com.java.kokodi.dto.TurnDto;
import com.java.kokodi.mapper.TurnMapper;
import com.java.kokodi.repository.TurnRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Сервис для работы с ходами игроков.
 * Обеспечивает создание и получение информации о ходе игроков в игровых сессиях.
 */
@Service
@RequiredArgsConstructor
public class TurnService {
    private final TurnRepository turnRepository;
    private final TurnMapper turnMapper;

    /**
     * Получает все ходы для указанной игровой сессии.
     *
     * @param gameId идентификатор игровой сессии
     * @return список DTO ходов в сессии
     */
    @Transactional(readOnly = true)
    public List<TurnDto> getGameTurns(UUID gameId) {
        return turnMapper.toDtoList(
                turnRepository.findTurnsForSession(gameId)
        );
    }

    /**
     * Получает ходы конкретного пользователя в указанной игровой сессии.
     *
     * @param gameId идентификатор игровой сессии
     * @param userId идентификатор пользователя
     * @return список DTO ходов пользователя, отсортированный по времени
     */
    @Transactional(readOnly = true)
    public List<TurnDto> getUserTurnsInGame(UUID gameId, UUID userId) {
        return turnMapper.toDtoList(
                turnRepository.findByGameSessionIdAndPlayerIdOrderByTimestampDesc(gameId, userId)
        );
    }

}
