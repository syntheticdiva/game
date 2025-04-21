package com.java.kokodi.service;

import com.java.kokodi.dto.GameSessionDto;
import com.java.kokodi.dto.GameStatusDto;
import com.java.kokodi.dto.PlayerScoreDto;
import com.java.kokodi.dto.TurnDto;
import com.java.kokodi.entity.Card;
import com.java.kokodi.entity.GameSession;
import com.java.kokodi.entity.User;
import com.java.kokodi.enums.GameStatus;
import com.java.kokodi.exception.GameException;
import com.java.kokodi.mapper.GameSessionMapper;
import com.java.kokodi.mapper.TurnMapper;
import com.java.kokodi.repository.CardRepository;
import com.java.kokodi.repository.GameSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Сервис для управления игровыми сессиями.
 * Обеспечивает создание игр, подключение игроков, обработку ходов и контроль игрового процесса.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GameService {
    private final GameSessionRepository gameSessionRepository;
    private final CardService cardService;
    private final UserService userService;
    private final GameSessionMapper gameSessionMapper;

    /**
     * Создает новую игровую сессию.
     *
     * @param creatorId идентификатор пользователя-создателя
     * @return DTO созданной игровой сессии
     * @throws GameException если пользователь не найден
     */
    @Transactional
    public GameSessionDto createGame(UUID creatorId) {
        User creator = userService.getEntityById(creatorId);
        GameSession gameSession = new GameSession();
        gameSession.setStatus(GameStatus.WAIT_FOR_PLAYERS);
        gameSession.addPlayer(creator);
        gameSession.setCurrentPlayerIndex(0);

        GameSession savedSession = gameSessionRepository.save(gameSession);
        return gameSessionMapper.toDto(savedSession);
    }

    /**
     * Подключает игрока к существующей сессии.
     *
     * @param gameId идентификатор игровой сессии
     * @param userId идентификатор подключаемого игрока
     * @return DTO обновленной игровой сессии
     * @throws GameException если игрок уже в игре, игра заполнена или сессия/игрок не найдены
     */
    @Transactional
    public GameSessionDto joinGame(UUID gameId, UUID userId) {
        GameSession gameSession = getGameSessionEntity(gameId);
        User user = userService.getEntityById(userId);

        if (gameSession.getPlayers().contains(user)) {
            throw new GameException("You are already in this game");
        }
        if (gameSession.getPlayers().size() >= 4) {
            throw new GameException("Game is full");
        }

        gameSession.addPlayer(user);
        return gameSessionMapper.toDto(gameSessionRepository.save(gameSession));
    }

    /**
     * Начинает игровую сессию.
     *
     * @param gameId идентификатор игровой сессии
     * @param userId идентификатор инициатора старта
     * @return DTO начатой игровой сессии
     * @throws GameException если:
     *                       - инициатор не является создателем
     *                       - игра уже начата
     *                       - недостаточно игроков (меньше 2)
     */
    @Transactional
    public GameSessionDto startGame(UUID gameId, UUID userId) {
        GameSession gameSession = getGameSessionEntity(gameId);
        User user = userService.getEntityById(userId);

        if (gameSession.getPlayers().isEmpty() ||
                !gameSession.getPlayers().get(0).getId().equals(user.getId())) {
            throw new GameException("Только создатель может начать игру");
        }

        if (gameSession.getStatus() != GameStatus.WAIT_FOR_PLAYERS) {
            throw new GameException("Игра уже начата");
        }

        if (gameSession.getPlayers().size() < 2) {
            throw new GameException("Для старта нужно минимум 2 игрока");
        }

        cardService.initializeDeck(gameSession);
        gameSession.setStatus(GameStatus.IN_PROGRESS);
        gameSession.setCurrentPlayerIndex(0);

        GameSession savedSession = gameSessionRepository.saveAndFlush(gameSession);
        return gameSessionMapper.toDto(savedSession);
    }

    /**
     * Обрабатывает ход игрока.
     *
     * @param gameId идентификатор игры
     * @param userId идентификатор игрока
     * @return DTO выполненного хода
     * @throws GameException если:
     *                       - игра завершена
     *                       - не ход игрока
     *                       - игрок/сессия не найдены
     */
    @Transactional
    public TurnDto playTurn(UUID gameId, UUID userId) {
        GameSession gameSession = getGameSessionEntity(gameId);
        User user = userService.getEntityById(userId);

        if (gameSession.getStatus() == GameStatus.FINISHED) {
            throw new GameException("Game is finished");
        }

        if (!gameSession.getCurrentPlayer().equals(user)) {
            throw new GameException("It's not your turn");
        }

        Card card = cardService.drawCard(gameSession);
        TurnDto turnDto = cardService.applyCardEffect(gameSession, card, user);

        if (checkWinCondition(gameSession)) {
            gameSession.setStatus(GameStatus.FINISHED);
            gameSession.setWinner(user);
        } else {
            gameSession.moveToNextPlayer();
        }

        gameSessionRepository.save(gameSession);
        return turnDto;
    }
    /**
     * Получает список всех активных игр
     *
     * @return список DTO активных игровых сессий
     */
    @Transactional(readOnly = true)
    public List<GameSessionDto> getAllActiveGames() {
        return gameSessionMapper.toDtoList(
                gameSessionRepository.findByStatusNot(GameStatus.FINISHED)
        );
    }

    /**
     * Получает детализированный статус игры.
     *
     * @param gameId идентификатор игры
     * @return DTO с подробной информацией о статусе игры
     */
    public GameStatusDto getDetailedStatus(UUID gameId) {
        GameSession gameSession = getGameSessionEntity(gameId);

        List<PlayerScoreDto> players = gameSession.getPlayers().stream()
                .map(player -> new PlayerScoreDto(
                        player.getId(),
                        player.getName(),
                        userService.getUserScore(player, gameSession))
                ).toList();

        return GameStatusDto.builder()
                .gameId(gameSession.getId())
                .status(gameSession.getStatus())
                .currentPlayer(gameSession.getCurrentPlayer().getId())
                .players(players)
                .cardsLeft((int) gameSession.getDeck().stream()
                        .filter(card -> !card.isPlayed()).count())
                .winner(gameSession.getWinner() != null ? gameSession.getWinner().getId() : null)
                .build();
    }

    /**
     * Проверяет условие победы в игре.
     *
     * @param gameSession игровая сессия
     * @return true если хотя бы один игрок набрал 30 или более очков
     */
    private boolean checkWinCondition(GameSession gameSession) {
        return gameSession.getPlayers().stream()
                .anyMatch(player -> userService.getUserScore(player, gameSession) >= 30);
    }

    /**
     * Получает сущность игровой сессии по идентификатору.
     *
     * @param gameId идентификатор игры
     * @return сущность игровой сессии
     * @throws GameException если сессия не найдена
     */
    private GameSession getGameSessionEntity(UUID gameId) {
        return gameSessionRepository.findById(gameId)
                .orElseThrow(() -> new GameException("Game session not found"));
    }
}