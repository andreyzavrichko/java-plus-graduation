package ru.practicum.ewm.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.exception.ServiceUnavailableException;
import ru.practicum.ewm.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.request.dto.ParticipationRequestDto;

import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class RequestFeignClientFallback implements RequestFeignClient {

    @Override
    public List<ParticipationRequestDto> getByEvent(Long eventId) {
        log.warn("request-service unavailable: getByEvent({}) — returning empty list", eventId);
        return List.of();
    }

    @Override
    public EventRequestStatusUpdateResult updateStatuses(
            Long userId, Long eventId, Integer participantLimit,
            EventRequestStatusUpdateRequest body) {
        log.error("request-service unavailable: updateStatuses(event={})", eventId);
        throw new ServiceUnavailableException(
                "Request service unavailable. Cannot update statuses for event %d".formatted(eventId));
    }

    @Override
    public Map<Long, Long> confirmedCounts(List<Long> eventIds) {
        log.warn("request-service unavailable: confirmedCounts — returning zeros");
        return Map.of();
    }
}
