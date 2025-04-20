package com.java.kokodi.mapper;

import com.java.kokodi.dto.GameSessionDto;
import com.java.kokodi.dto.UserDto;
import com.java.kokodi.entity.GameSession;
import com.java.kokodi.entity.User;
import com.java.kokodi.enums.GameStatus;
import org.mapstruct.Context;
import com.java.kokodi.enums.GameStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = UserMapper.class, imports = {GameStatus.class} )
public abstract class GameSessionMapper {

    @Autowired
    protected UserMapper userMapper;

    @Mapping(target = "players", source = "players")
    @Mapping(target = "currentPlayer", source = "currentPlayer")
    @Mapping(target = "nextPlayer", source = "gameSession", qualifiedByName = "mapNextPlayer")
    @Mapping(target = "cardsInDeck", expression = "java(gameSession.getDeck().size())")
    @Mapping(target = "canStart", expression = "java(gameSession.getStatus() == GameStatus.WAIT_FOR_PLAYERS && gameSession.getPlayers().size() >= 2)")
    @Mapping(target = "canJoin", expression = "java(gameSession.getStatus() == GameStatus.WAIT_FOR_PLAYERS && gameSession.getPlayers().size() < 4)")
    @Mapping(target = "finished", expression = "java(gameSession.getStatus() == GameStatus.FINISHED)")
    public abstract GameSessionDto toDto(GameSession gameSession);

    @Named("mapNextPlayer")
    protected UserDto mapNextPlayer(GameSession gameSession) {
        if (gameSession.getPlayers() == null
                || gameSession.getPlayers().isEmpty()
                || gameSession.getNextPlayIndex() == null) {
            return null;
        }
        int nextIndex = gameSession.getNextPlayIndex() % gameSession.getPlayers().size();
        return userMapper.toDto(gameSession.getPlayers().get(nextIndex));
    }

    public List<GameSessionDto> toDtoList(List<GameSession> gameSessions) {
        return gameSessions.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}