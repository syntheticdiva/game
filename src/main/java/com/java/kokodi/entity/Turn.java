package com.java.kokodi.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;

@Data
@Entity
@Table(name = "turns")
public class Turn {
    @Id
    @GeneratedValue
    private UUID id;
    @ManyToOne
    @JoinColumn(name = "game_session_id")
    private GameSession gameSession;
    @ManyToOne
    @JoinColumn(name = "player_id")
    private User player;
    @ManyToOne
    @JoinColumn(name = "card_id")
    private Card card;
    private String action;
    private LocalDateTime timestamp;
    private int scoreBefore;
    private int scoreAfter;


}
