package ru.practicum.ewm.user.service;

import ru.practicum.ewm.user.dto.NewUserRequest;
import ru.practicum.ewm.user.dto.UserDto;
import ru.practicum.ewm.user.dto.UserSearchRequest;

import java.util.List;

public interface UserService {
    List<UserDto> getUsers(UserSearchRequest request);

    UserDto createUser(NewUserRequest request);

    void deleteUser(Long userId);
}
