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
import ru.practicum.ewm.event.dto.params.EventParams;
import ru.practicum.ewm.event.dto.params.EventParamsSorted;
import ru.practicum.ewm.event.dto.projection.EventInfo;
import ru.practicum.ewm.event.dto.request.CreateEventBody;
import ru.practicum.ewm.event.dto.request.CreateEventDto;
import ru.practicum.ewm.event.dto.request.UpdateEventBody;
import ru.practicum.ewm.event.dto.request.UpdateEventDto;
import ru.practicum.ewm.event.service.EventService;
import ru.practicum.ewm.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.request.dto.ParticipationRequestDto;
import ru.practicum.ewm.request.dto.UpdateRequestStatusBody;
import ru.practicum.ewm.request.dto.UpdateRequestStatusDto;
import ru.practicum.ewm.sharing.constants.ApiPaths;

import java.util.List;

@RestController
@RequestMapping(ApiPaths.Private.EVENTS)
@Validated
@RequiredArgsConstructor
@Slf4j
public class PrivateEventController {
    private final EventService eventService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventDto create(
            @PathVariable @Positive Long userId,
            @RequestBody @Valid CreateEventBody body) {

        log.info("PRIVATE: Create EVENT request: {} for user ID {}", body, userId);
        CreateEventDto dto = CreateEventDto.of(body, userId);
        EventDto result = eventService.create(dto);
        log.info("PRIVATE: Created EVENT ID {} for user ID {}", result.id(), userId);
        return result;
    }

    @GetMapping
    public List<EventInfo> get(
            @PathVariable @Positive Long userId,
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(defaultValue = "10") @Positive Integer size) {

        log.info("PRIVATE: Get EVENTS for user ID {} with params: from={}, size={}", userId, from, size);
        EventParamsSorted params = EventParamsSorted.of(userId, from, size);
        List<EventInfo> result = eventService.get(params);
        log.info("PRIVATE: Found {} EVENTS for user ID {}", result.size(), userId);
        return result;
    }

    @GetMapping("/{eventId}")
    public EventDtoExtended get(
            @PathVariable @Positive Long userId,
            @PathVariable @Positive Long eventId) {

        log.info("PRIVATE: Get EVENT ID {} for user ID {}", eventId, userId);
        EventParams params = EventParams.of(userId, eventId);
        EventDtoExtended result = eventService.get(params);
        log.info("PRIVATE: Found EVENT ID {} for user ID {}", eventId, userId);
        return result;
    }

    @PatchMapping("/{eventId}")
    public EventDto update(
            @PathVariable @Positive Long userId,
            @PathVariable @Positive Long eventId,
            @RequestBody @Valid UpdateEventBody body) {

        log.info("PRIVATE: Update EVENT ID {} for user ID {}", eventId, userId);
        UpdateEventDto dto = UpdateEventDto.of(body, userId, eventId);
        EventDto result = eventService.update(dto);
        log.info("PRIVATE: Updated EVENT ID {} for user ID {}", eventId, userId);
        return result;
    }

    @GetMapping("/{eventId}/requests")
    public List<ParticipationRequestDto> getEventRequests(
            @PathVariable @Positive Long userId,
            @PathVariable @Positive Long eventId) {

        log.info("PRIVATE: Get Participation REQUESTS in EVENT with Id {} for user with Id {}", eventId, userId);
        EventParams params = EventParams.of(userId, eventId);
        List<ParticipationRequestDto> result = eventService.getEventRequests(params);
        log.info("PRIVATE: Found {} Participation REQUESTS in EVENT with Id {} for user with Id {}",
                result.size(), eventId, userId);
        return result;
    }

    @PatchMapping("/{eventId}/requests")
    public EventRequestStatusUpdateResult updateEventRequests(
            @PathVariable @Positive Long userId,
            @PathVariable @Positive Long eventId,
            @RequestBody @Valid UpdateRequestStatusBody body) {

        log.info("PRIVATE: Update REQUESTS with Ids {} in EVENT with Id {} for user with Id {}. Status: {}",
                body.requestIds(), eventId, userId, body.status());
        UpdateRequestStatusDto dto = UpdateRequestStatusDto.of(body, userId, eventId);
        EventRequestStatusUpdateResult result = eventService.updateEventRequestStatus(dto);
        log.info("PRIVATE: REQUESTS updated. Status CONFIRMED {} requests. Status REJECTED {} requests",
                result.confirmedRequests().size(), result.rejectedRequests().size());
        return result;
    }
}
