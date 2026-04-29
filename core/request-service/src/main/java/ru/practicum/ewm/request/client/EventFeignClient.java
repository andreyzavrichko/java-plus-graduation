package ru.practicum.ewm.request.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.api.dto.EventInternalDto;

@FeignClient(name = "main-service", fallback = EventFeignClientFallback.class)
public interface EventFeignClient {

    @GetMapping("/internal/events/{eventId}")
    EventInternalDto getEvent(@PathVariable("eventId") Long eventId);

    @GetMapping("/internal/events/exists/{eventId}")
    boolean exists(@PathVariable("eventId") Long eventId);
}
