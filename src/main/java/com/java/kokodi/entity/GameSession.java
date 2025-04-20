package com.java.kokodi.entity;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.java.kokodi.enums.GameStatus;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import com.vladmihalcea.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.hibernate.annotations.Type;


import java.util.*;

@Data
@Entity
@Table(name = "game_session")
public class GameSession {
    @Id
    @GeneratedValue
    private UUID id;
    @Enumerated(EnumType.STRING)
    private GameStatus status = GameStatus.WAIT_FOR_PLAYERS;

    @ManyToMany
    @JoinTable(name = "game_session_players",
    joinColumns = @JoinColumn(name = "game_session_id"),
    inverseJoinColumns = @JoinColumn(name = "user_id"))
    private List<User> players = new ArrayList<>();

    @OneToMany(mappedBy = "gameSession", cascade = CascadeType.ALL)
    private List<Card> deck = new ArrayList<>();
    @OneToMany(mappedBy = "gameSession", cascade = CascadeType.ALL)
    private List<Turn> turns = new ArrayList<>();

    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb")
    private Map<UUID, Integer> playerScores = new HashMap<>();

    private Integer currentPlayerIndex = 0;
    private Integer nextPlayIndex = 0;
    private boolean blockNextPlayer = false;



    public void addPlayer(User user){
        if (players.size()>=4){
            throw new IllegalStateException("Game session is full");
        }
        players.add(user);
        if (players.size() >= 2 && status == GameStatus.WAIT_FOR_PLAYERS) {
            status = GameStatus.IN_PROGRESS;
        }
        }
        public void moveToNextPlayer(){
        if (blockNextPlayer){
            nextPlayIndex = (currentPlayerIndex + 2 ) % players.size();
            blockNextPlayer = false;
        } else {
            nextPlayIndex = (currentPlayerIndex + 1) % players.size();
        }
        currentPlayerIndex = nextPlayIndex;

    }
    public User getCurrentPlayer() {
        if (players.isEmpty() || currentPlayerIndex >= players.size()) {
            return null;
        }
        return players.get(currentPlayerIndex);
    }
    public Integer getNextPlayIndex() {
        return this.nextPlayIndex;
    }
}
