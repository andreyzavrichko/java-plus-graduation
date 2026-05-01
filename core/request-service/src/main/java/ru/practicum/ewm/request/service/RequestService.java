package ru.practicum.ewm.request.service;

import ru.practicum.ewm.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.request.dto.ParticipationRequestDto;

import java.util.List;
import java.util.Map;

public interface RequestService {


    ParticipationRequestDto create(Long userId, Long eventId);

    List<ParticipationRequestDto> get(Long userId);

    ParticipationRequestDto cancel(Long userId, Long requestId);

    List<ParticipationRequestDto> getByEvent(Long eventId);

    EventRequestStatusUpdateResult updateStatuses(
            Long userId, Long eventId, Integer participantLimit,
            EventRequestStatusUpdateRequest request);

    Map<Long, Long> confirmedCounts(List<Long> eventIds);
}