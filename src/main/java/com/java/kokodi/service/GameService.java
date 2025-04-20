package com.java.kokodi.service;

import com.java.kokodi.dto.GameSessionDto;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GameService {
    private final GameSessionRepository gameSessionRepository;
    private final CardService cardService;
    private final UserService userService;
    private final GameSessionMapper gameSessionMapper;
    private final TurnMapper turnMapper;

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
    @Transactional
    public GameSessionDto joinGame(UUID gameId, UUID userId){
        GameSession gameSession = getGameSessionEntity(gameId);
        User user = userService.getEntityById(userId);
        if (gameSession.getPlayers().contains(user)){
            throw new GameException("You are already in this game");
        }
        if (gameSession.getPlayers().size()>=4){
            throw new GameException("Game is full");
        }
        gameSession.addPlayer(user);
        GameSession updateSession = gameSessionRepository.save(gameSession);
        return gameSessionMapper.toDto(updateSession);
    }

    @Transactional
    public GameSessionDto startGame(UUID gameId, UUID userId){
        GameSession gameSession = getGameSessionEntity(gameId);
        User user = userService.getEntityById(userId);
        if (!gameSession.getPlayers().get(0).equals(user)){
            throw new GameException("Only creator can start the game");
        }
        if (gameSession.getPlayers().size()<2){
            throw new GameException("Not enough players to start");
        }
        gameSession.setStatus(GameStatus.IN_PROGRESS);
        cardService.initializeDeck(gameSession);
        GameSession startedSession = gameSessionRepository.save(gameSession);
        return gameSessionMapper.toDto(startedSession);
    }
    @Transactional
    public TurnDto playTurn(UUID gameId, UUID userId){
        GameSession gameSession = getGameSessionEntity(gameId);
        User user = userService.getEntityById(userId);

        validateTurn(gameSession, user);

        Card card = cardService.drawCard(gameSession);
        TurnDto turnDto = cardService.applyCardEffect(gameSession, card, user);
        if (checkWinCondition(gameSession)){
            gameSession.setStatus(GameStatus.FINISHED);
            gameSessionRepository.save(gameSession);
        } else {
            gameSession.moveToNextPlayer();
            gameSessionRepository.save(gameSession);
        }
        return turnDto;
    }
    @Transactional(readOnly = true)
    public GameSessionDto getGameStatus(UUID gameId){
        return gameSessionMapper.toDto(getGameSessionEntity(gameId));
    }
    @Transactional(readOnly = true)
    public List<GameSessionDto> getAllActiveGames() {
        return gameSessionMapper.toDtoList( // Используем правильный метод
                gameSessionRepository.findByStatusNot(GameStatus.FINISHED)
        );
    }
    private void validateTurn(GameSession gameSession, User user){
        if (gameSession.getStatus()==GameStatus.FINISHED){
            throw new GameException("Game is already finished");
        }
        if (!gameSession.getCurrentPlayer().equals(user)){
            throw new GameException("It's not your turn");
        }
    }
    private boolean checkWinCondition(GameSession gameSession){
        return  gameSession.getPlayers().stream()
                .anyMatch(player->userService.getUserScore(player, gameSession)>=30);
    }
    private GameSession getGameSessionEntity(UUID gameId) {
        return gameSessionRepository.findById(gameId)
                .orElseThrow(() -> new GameException("Game session not found"));
    }

    }


