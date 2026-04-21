package ru.practicum.ewm.request.controller;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.request.dto.ParticipationRequestDto;
import ru.practicum.ewm.request.service.RequestService;
import ru.practicum.ewm.sharing.constants.ApiPaths;

import java.util.List;

@RestController
@RequestMapping(ApiPaths.Private.REQUESTS)
@Validated
@RequiredArgsConstructor
@Slf4j
public class RequestController {
    private final RequestService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto create(
            @PathVariable @Positive Long userId,
            @RequestParam @Positive Long eventId) {

        log.info("PRIVATE: Create REQUEST request: userId={}, eventId={}", userId, eventId);
        ParticipationRequestDto result = service.create(userId, eventId);
        log.info("PRIVATE: Created REQUEST: result={}", result);
        return result;
    }

    @GetMapping
    public List<ParticipationRequestDto> get(
            @PathVariable @Positive Long userId) {

        log.info("PRIVATE: Get REQUEST from User with Id {}", userId);
        List<ParticipationRequestDto> result = service.get(userId);
        log.info("PRIVATE: Found {} REQUESTS for User with Id {}", result.size(), userId);
        return service.get(userId);
    }

    @PatchMapping("/{requestId}/cancel")
    public ParticipationRequestDto cancel(
            @PathVariable @Positive Long userId,
            @PathVariable @Positive Long requestId) {

        log.info("PRIVATE: Cancel REQUEST with Id {} from User with Id {}", requestId, userId);
        ParticipationRequestDto result = service.cancel(userId, requestId);
        log.info("PRIVATE: Cancelled REQUEST with Id {} from User with Id {}", requestId, userId);
        return result;
    }
}
