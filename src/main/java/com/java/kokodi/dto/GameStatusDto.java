package com.java.kokodi.dto;

import com.java.kokodi.enums.GameStatus;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class GameStatusDto {
    private UUID gameId;
    private GameStatus status;
    private UUID currentPlayer;
    private List<PlayerScoreDto> players;
    private Integer cardsLeft;
    private UUID winner;
}