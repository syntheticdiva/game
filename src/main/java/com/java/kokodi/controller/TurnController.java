package com.java.kokodi.controller;

import com.java.kokodi.dto.TurnDto;
import com.java.kokodi.service.TurnService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/games/{gameId}/turns")
@RequiredArgsConstructor
public class TurnController {

    private final TurnService turnService;

    @GetMapping
    public ResponseEntity<List<TurnDto>> getGameTurns(@PathVariable UUID gameId) {
        return ResponseEntity.ok(turnService.getGameTurns(gameId));
    }

    @GetMapping("/players/{playerId}")
    public ResponseEntity<List<TurnDto>> getPlayerTurns(
            @PathVariable UUID gameId,
            @PathVariable UUID playerId) {
        return ResponseEntity.ok(turnService.getUserTurnsInGame(gameId, playerId));
    }
}
