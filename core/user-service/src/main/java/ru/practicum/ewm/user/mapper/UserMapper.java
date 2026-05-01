package ru.practicum.ewm.user.mapper;

import org.mapstruct.Mapper;
import ru.practicum.ewm.api.dto.UserInternalDto;
import ru.practicum.ewm.user.dto.*;
import ru.practicum.ewm.user.model.User;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDto toDto(User user);
    UserShortDto toShortDto(User user);
    User toEntity(NewUserRequest request);
    UserInternalDto toInternalDto(User user);
}
