package ru.practicum.ewm.request.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.request.dto.ParticipationRequestDto;
import ru.practicum.ewm.request.service.RequestService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/internal/requests")
@RequiredArgsConstructor
@Slf4j
public class InternalRequestController {

    private final RequestService service;

    @GetMapping("/events/{eventId}")
    public List<ParticipationRequestDto> getByEvent(@PathVariable Long eventId) {
        return service.getByEvent(eventId);
    }

    @PostMapping("/events/{eventId}/update-statuses")
    public EventRequestStatusUpdateResult updateStatuses(
            @RequestParam Long userId,
            @PathVariable Long eventId,
            @RequestParam(defaultValue = "0") Integer participantLimit,
            @RequestBody EventRequestStatusUpdateRequest body) {
        return service.updateStatuses(userId, eventId, participantLimit, body);
    }

    @PostMapping("/confirmed-counts")
    public Map<Long, Long> confirmedCounts(@RequestBody List<Long> eventIds) {
        return service.confirmedCounts(eventIds);
    }

    @GetMapping("/events/{eventId}/confirmed/{userId}")
    public boolean hasConfirmedRequest(
            @PathVariable Long eventId,
            @PathVariable Long userId) {
        return service.hasConfirmedRequest(eventId, userId);
    }
}