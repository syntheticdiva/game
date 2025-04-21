package com.java.kokodi.entity;


import com.java.kokodi.enums.CardType;
import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Data
@Entity
@Table(name = "cards")
public class Card {
    @Id
    @GeneratedValue
    private UUID uuid;
    @Column(nullable = false)
    private String name;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CardType type;
    @Column(nullable = false)
    private Integer value;
    @Column(name = "order_index")
    private Integer orderIndex;
    @ManyToOne
    @JoinColumn(name = "game_session_id")
    private GameSession gameSession;
    @ManyToOne
    @JoinColumn(name = "played_by_id")
    private User playedBy;
    private boolean played = false;

}
