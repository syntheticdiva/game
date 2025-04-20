package com.java.kokodi.repository;

import com.java.kokodi.entity.GameSession;
import com.java.kokodi.enums.GameStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GameSessionRepository extends JpaRepository<GameSession, UUID> {
    List<GameSession> findByStatusNot(GameStatus status);
}
