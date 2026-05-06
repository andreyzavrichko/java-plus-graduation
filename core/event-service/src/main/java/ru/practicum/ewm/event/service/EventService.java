package ru.practicum.ewm.event.service;

import ru.practicum.ewm.event.dto.EventDto;
import ru.practicum.ewm.event.dto.EventDtoExtended;
import ru.practicum.ewm.event.dto.EventDtoShort;
import ru.practicum.ewm.event.dto.EventDtoShortWithoutViews;
import ru.practicum.ewm.event.dto.params.AdminSearchParams;
import ru.practicum.ewm.event.dto.params.EventParams;
import ru.practicum.ewm.event.dto.params.EventParamsSorted;
import ru.practicum.ewm.event.dto.params.PublicSearchParams;
import ru.practicum.ewm.event.dto.request.CreateEventDto;
import ru.practicum.ewm.event.dto.request.UpdateEventDto;
import ru.practicum.ewm.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.request.dto.ParticipationRequestDto;

import java.util.List;

public interface EventService {

    EventDto create(CreateEventDto dto);

    EventDto update(UpdateEventDto dto);

    EventDto adminUpdate(UpdateEventDto dto);

    EventDtoExtended get(Long id);

    EventDtoExtended getWithUserId(Long id, Long userId);

    EventDtoExtended get(EventParams params);

    List<EventDtoShortWithoutViews> get(EventParamsSorted params);

    List<EventDtoExtended> get(AdminSearchParams params);

    List<EventDtoShort> get(PublicSearchParams params);

    List<EventDtoShort> getRecommendations(Long userId);

    void likeEvent(Long eventId, Long userId);

    List<ParticipationRequestDto> getEventRequests(EventParams params);

    EventRequestStatusUpdateResult updateEventRequestStatus(
            Long userId, Long eventId, EventRequestStatusUpdateRequest body);
}