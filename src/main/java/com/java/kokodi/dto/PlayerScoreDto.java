package com.java.kokodi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class PlayerScoreDto {
    private UUID playerId;
    private String playerName;
    private Integer score;
}
