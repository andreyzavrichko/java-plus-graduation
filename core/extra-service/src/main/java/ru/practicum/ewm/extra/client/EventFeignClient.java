package ru.practicum.ewm.extra.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.practicum.ewm.api.dto.EventInternalDto;

import java.util.List;
import java.util.Map;

@FeignClient(name = "main-service", fallback = EventFeignClientFallback.class)
public interface EventFeignClient {
    @GetMapping("/internal/events/{eventId}")
    EventInternalDto getEvent(@PathVariable("eventId") Long eventId);

    @PostMapping("/internal/events/batch")
    Map<Long, EventInternalDto> getEventsBatch(@RequestBody List<Long> ids);

    @GetMapping("/internal/events/exists/{eventId}")
    boolean exists(@PathVariable("eventId") Long eventId);
}
