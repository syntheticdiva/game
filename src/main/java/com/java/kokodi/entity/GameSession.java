package com.java.kokodi.entity;

import com.java.kokodi.enums.GameStatus;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import org.hibernate.annotations.Type;

import java.util.*;

/**
 * Класс, представляющий игровую сессию.
 * Содержит информацию об игроках, состоянии игры, колоде карт, ходе игры и результатах.
 */
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

    /**
     * Колода карт в текущей игровой сессии.
     */
    @OneToMany(
            mappedBy = "gameSession",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderColumn(name = "order_index")
    private List<Card> deck = new ArrayList<>();

    /**
     * Список ходов, сделанных в данной игровой сессии.
     */
    @OneToMany(mappedBy = "gameSession", cascade = CascadeType.ALL)
    private List<Turn> turns = new ArrayList<>();

    /**
     * Счет игроков в формате JSON.
     * Ключ - UUID игрока, значение - текущий счет.
     */
    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb")
    private Map<UUID, Integer> playerScores = new HashMap<>();

    /**
     * Победитель игровой сессии (если игра завершена).
     * Связь многие-к-одному с сущностью User.
     */
    @ManyToOne
    @JoinColumn(name = "winner_id")
    private User winner;

    /**
     * Индекс текущего игрока в списке players.
     * Используется для определения, чей сейчас ход.
     */
    private Integer currentPlayerIndex = 0;

    /**
     * Индекс следующего игрока в списке players.
     * Используется для определения, кто ходит следующим.
     */
    @Getter
    private Integer nextPlayIndex = 0;

    /**
     * Флаг, указывающий нужно ли пропустить следующего игрока.
     * Если true, следующий игрок будет пропущен (например, после блокирующей карты).
     */
    private boolean blockNextPlayer = false;

    /**
     * Добавляет игрока в сессию.
     * @param user Игрок для добавления
     * @throws IllegalStateException если в сессии уже максимальное количество игроков (4)
     */
    public void addPlayer(User user) {
        if (players.size() >= 4) {
            throw new IllegalStateException("Игровая сессия заполнена");
        }
        players.add(user);
        playerScores.put(user.getId(), 0);
    }

    /**
     * Передает ход следующему игроку.
     * Учитывает флаг blockNextPlayer для пропуска игрока при необходимости.
     */
    public void moveToNextPlayer() {
        if (blockNextPlayer) {
            nextPlayIndex = (currentPlayerIndex + 2) % players.size();
            blockNextPlayer = false;
        } else {
            nextPlayIndex = (currentPlayerIndex + 1) % players.size();
        }
        currentPlayerIndex = nextPlayIndex;
    }

    /**
     * Возвращает текущего игрока (чей сейчас ход).
     * @return Текущий игрок или null если игроков нет или неккоректныйа индекс
     */
    public User getCurrentPlayer() {
        if (players.isEmpty() || currentPlayerIndex >= players.size()) {
            return null;
        }
        return players.get(currentPlayerIndex);
    }

    /**
     * Возвращает следующего игрока (кто ходит следующим).
     * @return Следующий игрок
     */
    public User getNextPlayer() {
        int nextIndex = calculateNextIndex();
        return players.get(nextIndex);
    }

    /**
     * Вычисляет индекс следующего игрока с учетом флага blockNextPlayer.
     * @return Индекс следующего игрока в списке players
     */
    private int calculateNextIndex() {
        if (blockNextPlayer) {
            return (currentPlayerIndex + 2) % players.size();
        }
        return (currentPlayerIndex + 1) % players.size();
    }
}