package ru.practicum.ewm.request.controller;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.request.dto.ParticipationRequestDto;
import ru.practicum.ewm.request.service.RequestService;
import java.util.List;

@RestController
@RequestMapping("/users/{userId}/requests")
@RequiredArgsConstructor
@Validated
@Slf4j
public class RequestController {
    private final RequestService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto create(
            @PathVariable @Positive Long userId,
            @RequestParam @Positive Long eventId) {
        log.info("Create request: userId={}, eventId={}", userId, eventId);
        return service.create(userId, eventId);
    }

    @GetMapping
    public List<ParticipationRequestDto> get(@PathVariable @Positive Long userId) {
        return service.get(userId);
    }

    @PatchMapping("/{requestId}/cancel")
    public ParticipationRequestDto cancel(
            @PathVariable @Positive Long userId,
            @PathVariable @Positive Long requestId) {
        log.info("Cancel request: userId={}, requestId={}", userId, requestId);
        return service.cancel(userId, requestId);
    }
}
