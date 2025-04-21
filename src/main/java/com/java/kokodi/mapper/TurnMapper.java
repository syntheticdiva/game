package com.java.kokodi.mapper;

import com.java.kokodi.dto.TurnDto;
import com.java.kokodi.entity.Turn;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {UserMapper.class, CardMapper.class})
public interface TurnMapper {

    @Mapping(target = "cardName", source = "card.name")
    @Mapping(target = "cardType", source = "card.type")
    @Mapping(target = "playerName", source = "player.name")
    @Mapping(target = "nextPlayerId", source = "gameSession.nextPlayer.id")
    @Mapping(target = "nextPlayerName", source = "gameSession.nextPlayer.name")
    TurnDto toDto(Turn turn);

    @Mapping(target = "scoreBefore", source = "scoreBefore")
    @Mapping(target = "scoreAfter", source = "scoreAfter")
    TurnDto toDtoWithScores(Turn turn, int scoreBefore, int scoreAfter);

    List<TurnDto> toDtoList(List<Turn> turns);
}