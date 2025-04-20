package com.java.kokodi.repository;

import com.java.kokodi.entity.Turn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TurnRepository extends JpaRepository<Turn, UUID> {
    List<Turn> findByGameSessionIdAndPlayerIdOrderByTimestampDesc(UUID gameId, UUID userId);


    @Query("SELECT t FROM Turn t WHERE t.gameSession.id = :sessionId ORDER BY t.timestamp DESC")
    List<Turn> findTurnsForSession(@Param("sessionId") UUID sessionId);



}
