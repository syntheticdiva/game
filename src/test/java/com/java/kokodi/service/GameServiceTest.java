package com.java.kokodi.service;

import com.java.kokodi.dto.GameSessionDto;
import com.java.kokodi.dto.TurnDto;
import com.java.kokodi.entity.Card;
import com.java.kokodi.entity.GameSession;
import com.java.kokodi.entity.User;
import com.java.kokodi.enums.CardType;
import com.java.kokodi.enums.GameStatus;
import com.java.kokodi.mapper.GameSessionMapper;
import com.java.kokodi.repository.GameSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameServiceUnitTest {

    @Mock
    private CardService cardService;
    @Mock
    private UserService userService;
    @Mock
    private GameSessionRepository gameRepo;
    @Mock
    private GameSessionMapper gameMapper;
    @InjectMocks
    private GameService gameService;
    @BeforeEach
    void setUp() {
        gameRepo = mock(GameSessionRepository.class);
        cardService = mock(CardService.class);
        userService = mock(UserService.class);
        gameMapper = mock(GameSessionMapper.class);
        gameService = new GameService(gameRepo, cardService, userService, gameMapper);
    }
    @Test
    void createGameTest() {
        User creator = new User();
        creator.setId(UUID.randomUUID());

        when(userService.getEntityById(creator.getId())).thenReturn(creator);

        GameSession savedSession = new GameSession();
        savedSession.addPlayer(creator);
        when(gameRepo.save(any())).thenReturn(savedSession);

        GameSessionDto gameSessionDto = new GameSessionDto();
        when(gameMapper.toDto(savedSession)).thenReturn(gameSessionDto);

        GameSessionDto result = gameService.createGame(creator.getId());

        assertSame(gameSessionDto, result);
    }
    @Test
    void joinGame_ShouldAddPlayer() {
        UUID gameId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        User player = new User();
        player.setId(userId);

        GameSession gameSession = new GameSession();
        gameSession.setId(gameId);
        gameSession.setStatus(GameStatus.WAIT_FOR_PLAYERS);

        when(gameRepo.findById(gameId)).thenReturn(Optional.of(gameSession));
        when(userService.getEntityById(userId)).thenReturn(player);
        when(gameRepo.save(gameSession)).thenReturn(gameSession);

        GameSessionDto expectedDto = new GameSessionDto();
        expectedDto.setId(gameId);
        when(gameMapper.toDto(gameSession)).thenReturn(expectedDto);

        GameSessionDto result = gameService.joinGame(gameId, userId);

        assertNotNull(result);
        assertEquals(gameId, result.getId());
        assertEquals(1, gameSession.getPlayers().size());
        assertEquals(userId, gameSession.getPlayers().get(0).getId());

        verify(gameRepo).findById(gameId);
        verify(userService).getEntityById(userId);
        verify(gameRepo).save(gameSession);
        verify(gameMapper).toDto(gameSession);
    }
    @Test
    void playTurnWithStealCard() {
        UUID gameId = UUID.randomUUID();
        UUID currentPlayerId = UUID.randomUUID();
        UUID opponentId = UUID.randomUUID();

        User currentPlayer = new User();
        currentPlayer.setId(currentPlayerId);
        currentPlayer.setName("Игрок 1");

        User opponent = new User();
        opponent.setId(opponentId);
        opponent.setName("Игрок 2");

        GameSession game = new GameSession();
        game.setId(gameId);
        game.setStatus(GameStatus.IN_PROGRESS);
        game.setPlayers(List.of(currentPlayer, opponent));
        game.setCurrentPlayerIndex(0);

        Card stealCard = new Card();
        stealCard.setName("Steal");
        stealCard.setType(CardType.ACTION);
        stealCard.setValue(3);

        when(gameRepo.findById(gameId)).thenReturn(Optional.of(game));
        when(userService.getEntityById(currentPlayerId)).thenReturn(currentPlayer);
        when(cardService.drawCard(game)).thenReturn(stealCard);

        when(userService.addScore(currentPlayer, game, 3)).thenReturn(8);
        when(userService.addScore(opponent, game, -3)).thenReturn(7);

        when(cardService.applyCardEffect(game, stealCard, currentPlayer))
                .thenAnswer(invocation -> {
                    userService.addScore(currentPlayer, game, 3);
                    userService.addScore(opponent, game, -3);
                    return TurnDto.builder()
                            .cardName("Steal")
                            .action("Игрок 1 украл 3 очка у Игрок 2")
                            .scoreBefore(5)
                            .scoreAfter(8)
                            .nextPlayerId(opponentId)
                            .build();
                });

        TurnDto result = gameService.playTurn(gameId, currentPlayerId);
        verify(userService).addScore(currentPlayer, game, 3);
        verify(userService).addScore(opponent, game, -3);
        verify(gameRepo).save(game);
        assertNotNull(result);

    }

}

