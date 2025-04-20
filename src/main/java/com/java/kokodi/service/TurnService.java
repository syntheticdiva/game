package com.java.kokodi.service;

import com.java.kokodi.dto.TurnDto;
import com.java.kokodi.entity.Card;
import com.java.kokodi.entity.GameSession;
import com.java.kokodi.entity.Turn;
import com.java.kokodi.entity.User;
import com.java.kokodi.mapper.TurnMapper;
import com.java.kokodi.repository.TurnRepository;
import jakarta.persistence.Table;

import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TurnService {
    private final TurnRepository turnRepository;
    private final TurnMapper turnMapper;

    @Transactional(readOnly = true)
    public List<TurnDto> getGameTurns(UUID gameId) {
        return turnMapper.toDtoList(
                turnRepository.findTurnsForSession(gameId)
        );
    }

    @Transactional(readOnly = true)
    public List<TurnDto> getUserTurnsInGame(UUID gameId, UUID userId){
        return turnMapper.toDtoList(
                turnRepository.findByGameSessionIdAndPlayerIdOrderByTimestampDesc(gameId, userId)
        );
    }

    @Transactional
    public TurnDto createTurn(GameSession gameSession, User player, Card card, String action){
        Turn turn = new Turn();
        turn.setGameSession(gameSession);
        turn.setPlayer(player);
        turn.setCard(card);
        turn.setAction(action);

        Turn savedTurn = turnRepository.save(turn);
        return turnMapper.toDto(savedTurn);
    }
}
