package ru.practicum.ewm.event.controller;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.event.dto.EventDtoExtended;
import ru.practicum.ewm.event.dto.EventDtoShort;
import ru.practicum.ewm.event.dto.params.PublicSearchParams;
import ru.practicum.ewm.event.service.EventService;
import ru.practicum.ewm.sharing.constants.ApiPaths;

import java.util.List;

@RestController
@RequestMapping(ApiPaths.Public.EVENTS)
@RequiredArgsConstructor
@Validated
@Slf4j
public class PublicEventController {

    private final EventService eventService;

    @GetMapping
    public List<EventDtoShort> getEvents(
            @RequestParam(required = false) String text,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) Boolean paid,
            @RequestParam(required = false) String rangeStart,
            @RequestParam(required = false) String rangeEnd,
            @RequestParam(required = false) Boolean onlyAvailable,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(defaultValue = "10") @Positive Integer size) {
        log.info("PUBLIC: Get events, from={}, size={}", from, size);
        return eventService.get(PublicSearchParams.of(
                text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort, from, size));
    }


    @GetMapping("/{id}")
    public EventDtoExtended getEvent(
            @PathVariable @Positive Long id,
            @RequestHeader(value = "X-EWM-USER-ID", required = false) Long userId) {
        log.info("PUBLIC: Get event {}, userId={}", id, userId);
        if (userId != null) {
            return eventService.getWithUserId(id, userId);
        }
        return eventService.get(id);
    }


    @GetMapping("/recommendations")
    public List<EventDtoShort> getRecommendations(
            @RequestHeader("X-EWM-USER-ID") Long userId) {
        log.info("PUBLIC: Get recommendations for userId={}", userId);
        return eventService.getRecommendations(userId);
    }


    @PutMapping("/{eventId}/like")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void likeEvent(
            @PathVariable @Positive Long eventId,
            @RequestHeader("X-EWM-USER-ID") Long userId) {
        log.info("PUBLIC: Like event {} by userId={}", eventId, userId);
        eventService.likeEvent(eventId, userId);
    }
}