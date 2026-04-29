package ru.practicum.ewm.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.request.dto.ParticipationRequestDto;

import java.util.List;
import java.util.Map;

@FeignClient(name = "request-service", fallback = RequestFeignClientFallback.class)
public interface RequestFeignClient {

    @GetMapping("/internal/requests/events/{eventId}")
    List<ParticipationRequestDto> getByEvent(@PathVariable("eventId") Long eventId);

    @PostMapping("/internal/requests/events/{eventId}/update-statuses")
    EventRequestStatusUpdateResult updateStatuses(
            @RequestParam("userId") Long userId,
            @PathVariable("eventId") Long eventId,
            @RequestParam("participantLimit") Integer participantLimit,
            @RequestBody EventRequestStatusUpdateRequest body);

    @PostMapping("/internal/requests/confirmed-counts")
    Map<Long, Long> confirmedCounts(@RequestBody List<Long> eventIds);
}
