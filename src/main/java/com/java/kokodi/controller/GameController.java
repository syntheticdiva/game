package com.java.kokodi.controller;


import com.java.kokodi.dto.GameSessionDto;
import com.java.kokodi.dto.GameStatusDto;
import com.java.kokodi.dto.TurnDto;
import com.java.kokodi.entity.User;
import com.java.kokodi.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/games")
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;

    @PostMapping("/{gameId}/create new game")
    public ResponseEntity<GameSessionDto> createGame(
            @AuthenticationPrincipal User user) {
        UUID userId = user.getId();
        return ResponseEntity.ok(gameService.createGame(userId));
    }
    @PostMapping("/{gameId}/join")
    public ResponseEntity<GameSessionDto> joinGame(
            @PathVariable UUID gameId,
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = ((User) userDetails).getId();
        return ResponseEntity.ok(gameService.joinGame(gameId, userId));
    }

    @PostMapping("/{gameId}/start")
    public ResponseEntity<GameSessionDto> startGame(
            @PathVariable UUID gameId,
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = ((User) userDetails).getId();
        return ResponseEntity.ok(gameService.startGame(gameId, userId));
    }

    @PostMapping("/{gameId}/turn")
    public ResponseEntity<TurnDto> playTurn(
            @PathVariable UUID gameId,
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = ((User) userDetails).getId();
        return ResponseEntity.ok(gameService.playTurn(gameId, userId));
    }

    @GetMapping
    public ResponseEntity<List<GameSessionDto>> getActiveGames() {
        return ResponseEntity.ok(gameService.getAllActiveGames());
    }
    @GetMapping("/{gameId}/status")
    public ResponseEntity<GameStatusDto> getGameStatus(@PathVariable UUID gameId) {
        return ResponseEntity.ok(gameService.getDetailedStatus(gameId));
    }
}
