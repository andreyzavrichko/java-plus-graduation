package ru.practicum.ewm.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.api.dto.UserInternalDto;
import ru.practicum.ewm.user.service.UserService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/internal/users")
@RequiredArgsConstructor
public class InternalUserController {

    private final UserService userService;

    @GetMapping("/{userId}")
    public UserInternalDto getUser(@PathVariable Long userId) {
        return userService.getUser(userId);
    }

    @PostMapping("/batch")
    public Map<Long, UserInternalDto> getUsersBatch(@RequestBody List<Long> ids) {
        return userService.getUsersBatch(ids);
    }

    @GetMapping("/exists/{userId}")
    public boolean exists(@PathVariable Long userId) {
        return userService.exists(userId);
    }
}