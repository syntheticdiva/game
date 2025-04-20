package com.java.kokodi.mapper;

import com.java.kokodi.dto.UserDto;
import com.java.kokodi.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "password", ignore = true) // Игнорируем пароль при преобразовании в DTO
    UserDto toDto(User user);

    // Убрано игнорирование gameSessions, так как поля больше нет
    User toEntity(UserDto userDto);

    List<UserDto> toDtoList(List<User> users);
}