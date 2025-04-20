package com.java.kokodi.dto;

import com.java.kokodi.enums.CardType;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class CardDto {
    private UUID uuid; // добавлено
    private String name;
    private CardType type;
    private Integer value;
}
//@Data
//@Builder
//public class CardDto {
//    private String name;
//    private CardType type;
//    private Integer value;
//}
