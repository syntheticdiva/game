package com.java.kokodi.dto;

import com.java.kokodi.enums.CardType;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class TurnDto {
    private String cardName;
    private CardType cardType;
    private String action;
    private String playerName;
    private int scoreBefore;
    private int scoreAfter;
    private UUID nextPlayerId;
    private String nextPlayerName;
}
