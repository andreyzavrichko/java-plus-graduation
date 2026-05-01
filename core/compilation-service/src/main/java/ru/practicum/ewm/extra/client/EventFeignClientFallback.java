package ru.practicum.ewm.extra.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.api.dto.EventInternalDto;
import ru.practicum.ewm.extra.exception.ServiceUnavailableException;

import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class EventFeignClientFallback implements EventFeignClient {

    @Override
    public EventInternalDto getEvent(Long eventId) {
        log.error("main-service unavailable: getEvent({})", eventId);
        throw new ServiceUnavailableException(
                "Event service unavailable. Cannot fetch event %d".formatted(eventId));
    }

    @Override
    public Map<Long, EventInternalDto> getEventsBatch(List<Long> ids) {
        log.warn("main-service unavailable: getEventsBatch — returning empty map");
        return Map.of();
    }

    @Override
    public boolean exists(Long eventId) {
        log.warn("main-service unavailable: exists({}) — returning false", eventId);
        return false;
    }
}
