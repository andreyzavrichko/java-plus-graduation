package ru.practicum.ewm.event.controller;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.event.dto.EventDtoExtended;
import ru.practicum.ewm.event.dto.EventDtoShort;
import ru.practicum.ewm.event.dto.params.PublicSearchParams;
import ru.practicum.ewm.event.service.EventService;
import ru.practicum.ewm.sharing.constants.ApiPaths;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiPaths.Public.EVENTS)
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

        log.info("PUBLIC: Get EVENTS with params [text '{}', categories '{}', paid '{}', rangeStart '{}', " +
                        "rangeEnd '{}',  onlyAvailable '{}', sort '{}', from '{}', size '{}']",
                text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort, from, size);
        PublicSearchParams params = PublicSearchParams.of(text, categories, paid, rangeStart, rangeEnd, onlyAvailable,
                sort, from, size);
        List<EventDtoShort> result = eventService.get(params);
        log.info("PUBLIC: Found {} EVENTS", result.size());
        return result;
    }

    @GetMapping("/{id}")
    public EventDtoExtended getEvent(@PathVariable @Positive Long id) {

        log.info("PUBLIC: Get EVENT with commentId {}", id);
        EventDtoExtended result = eventService.get(id);
        log.info("PUBLIC: EVENT with commentId {} found", id);
        return result;
    }
}
