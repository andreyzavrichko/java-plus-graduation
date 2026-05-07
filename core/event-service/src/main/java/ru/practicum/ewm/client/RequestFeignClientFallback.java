package ru.practicum.ewm.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
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
        log.warn("request-service unavailable: getByEvent({}) fallback", eventId);
        return List.of();
    }

    @Override
    public EventRequestStatusUpdateResult updateStatuses(Long userId, Long eventId,
                                                         Integer participantLimit,
                                                         EventRequestStatusUpdateRequest body) {
        log.warn("request-service unavailable: updateStatuses fallback");
        return new EventRequestStatusUpdateResult(List.of(), List.of());
    }

    @Override
    public Map<Long, Long> confirmedCounts(List<Long> eventIds) {
        log.warn("request-service unavailable: confirmedCounts fallback");
        return Map.of();
    }

    @Override
    public boolean hasConfirmedRequest(Long eventId, Long userId) {
        log.warn("request-service unavailable: hasConfirmedRequest({},{}) fallback → false", eventId, userId);
        return false;
    }
}