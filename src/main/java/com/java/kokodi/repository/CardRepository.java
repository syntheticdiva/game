package com.java.kokodi.repository;

import com.java.kokodi.entity.Card;
import com.java.kokodi.entity.GameSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CardRepository extends JpaRepository<Card, UUID> {
    @Modifying
    @Query("DELETE FROM Card c WHERE c.gameSession = :gameSession")
    void deleteAllByGameSession(GameSession gameSession);
}
