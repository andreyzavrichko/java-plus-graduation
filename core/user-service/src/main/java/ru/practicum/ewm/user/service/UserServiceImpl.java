package ru.practicum.ewm.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.api.dto.UserInternalDto;
import ru.practicum.ewm.api.sharing.PageableFactory;
import ru.practicum.ewm.user.dto.NewUserRequest;
import ru.practicum.ewm.user.dto.UserDto;
import ru.practicum.ewm.user.dto.UserSearchRequest;
import ru.practicum.ewm.user.exception.ConflictException;
import ru.practicum.ewm.user.exception.NotFoundException;
import ru.practicum.ewm.user.mapper.UserMapper;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repository.UserRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;


    @Override
    public List<UserDto> getUsers(UserSearchRequest request) {
        Pageable pageable = PageableFactory.offset(request.from(), request.size());
        Page<User> page = (request.ids() == null || request.ids().isEmpty())
                ? userRepository.findAll(pageable)
                : userRepository.findAllByIdIn(request.ids(), pageable);
        return page.stream().map(userMapper::toDto).toList();
    }

    @Override
    @Transactional
    public UserDto createUser(NewUserRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("Email already exists: " + request.email());
        }
        return userMapper.toDto(userRepository.save(userMapper.toEntity(request)));
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User %d not found".formatted(userId));
        }
        userRepository.deleteById(userId);
    }


    @Override
    public UserInternalDto getUser(Long userId) {
        return userRepository.findById(userId)
                .map(userMapper::toInternalDto)
                .orElseThrow(() -> new NotFoundException(
                        "User %d not found".formatted(userId)));
    }

    @Override
    public Map<Long, UserInternalDto> getUsersBatch(List<Long> ids) {
        return userRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(User::getId, userMapper::toInternalDto));
    }

    @Override
    public boolean exists(Long userId) {
        return userRepository.existsById(userId);
    }
}