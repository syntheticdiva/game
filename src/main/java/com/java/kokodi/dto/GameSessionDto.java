package com.java.kokodi.dto;

import com.java.kokodi.enums.GameStatus;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class GameSessionDto {
    private UUID id;
    private GameStatus status;
    private List<UserDto> players;
    private int cardsInDeck;
    private UserDto currentPlayer;
    private UserDto nextPlayer;
    private boolean canStart;
    private boolean canJoin;
    private boolean finished;

}
