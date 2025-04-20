package com.java.kokodi.mapper;


import com.java.kokodi.dto.CardDto;
import com.java.kokodi.entity.Card;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CardMapper {

    CardDto toDto(Card card);

    Card toEntity(CardDto cardDto);
}
