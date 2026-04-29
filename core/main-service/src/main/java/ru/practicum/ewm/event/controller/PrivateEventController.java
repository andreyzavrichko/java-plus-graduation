package ru.practicum.ewm.event.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.event.dto.EventDto;
import ru.practicum.ewm.event.dto.EventDtoExtended;
import ru.practicum.ewm.event.dto.EventDtoShortWithoutViews;
import ru.practicum.ewm.event.dto.params.EventParams;
import ru.practicum.ewm.event.dto.params.EventParamsSorted;
import ru.practicum.ewm.event.dto.request.CreateEventBody;
import ru.practicum.ewm.event.dto.request.CreateEventDto;
import ru.practicum.ewm.event.dto.request.UpdateEventBody;
import ru.practicum.ewm.event.dto.request.UpdateEventDto;
import ru.practicum.ewm.event.service.EventService;
import ru.practicum.ewm.exception.ValidationException;
import ru.practicum.ewm.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.request.dto.ParticipationRequestDto;
import ru.practicum.ewm.sharing.constants.ApiPaths;

import java.util.List;

@RestController
@RequestMapping(ApiPaths.Private.EVENTS)
@RequiredArgsConstructor
@Validated
@Slf4j
public class PrivateEventController {
    private final EventService eventService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventDto create(
            @PathVariable @Positive Long userId,
            @RequestBody @Valid CreateEventBody body) {
        log.info("PRIVATE: Create event, userId={}", userId);
        return eventService.create(CreateEventDto.of(body, userId));
    }

    @GetMapping
    public List<EventDtoShortWithoutViews> get(
            @PathVariable @Positive Long userId,
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(defaultValue = "10") @Positive Integer size) {
        return eventService.get(EventParamsSorted.of(userId, from, size));
    }

    @GetMapping("/{eventId}")
    public EventDtoExtended get(
            @PathVariable @Positive Long userId,
            @PathVariable @Positive Long eventId) {
        return eventService.get(EventParams.of(userId, eventId));
    }

    @PatchMapping("/{eventId}")
    public EventDto update(
            @PathVariable @Positive Long userId,
            @PathVariable @Positive Long eventId,
            @RequestBody @Valid UpdateEventBody body) {
        log.info("PRIVATE: Update event {}, userId={}", eventId, userId);
        return eventService.update(UpdateEventDto.of(body, userId, eventId));
    }

    @GetMapping("/{eventId}/requests")
    public List<ParticipationRequestDto> getEventRequests(
            @PathVariable @Positive Long userId,
            @PathVariable @Positive Long eventId) {
        return eventService.getEventRequests(EventParams.of(userId, eventId));
    }

    @PatchMapping("/{eventId}/requests")
    public EventRequestStatusUpdateResult updateEventRequests(
            @PathVariable @Positive Long userId,
            @PathVariable @Positive Long eventId,
            @RequestBody(required = false) EventRequestStatusUpdateRequest body) {
        if (body == null) {
            throw new ValidationException("Request body is required");
        }
        log.info("PRIVATE: Update request statuses, eventId={}, userId={}", eventId, userId);
        return eventService.updateEventRequestStatus(userId, eventId, body);
    }
}
