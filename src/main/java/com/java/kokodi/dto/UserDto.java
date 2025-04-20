package com.java.kokodi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;
@Data
@Builder
public class UserDto {
    private UUID id;
    private String login;
    private String name;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) // Поле игнорируется при сериализации в JSON
    private String password;
}
//@Data
//@Builder
//public class UserDto {
//    private UUID id;
//    private String login;
//    private String name;
//    private String password;
//}
