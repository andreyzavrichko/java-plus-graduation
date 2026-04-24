package ru.practicum.ewm.event.service;

import ru.practicum.ewm.event.dto.EventDto;
import ru.practicum.ewm.event.dto.EventDtoExtended;
import ru.practicum.ewm.event.dto.EventDtoShort;
import ru.practicum.ewm.event.dto.params.AdminSearchParams;
import ru.practicum.ewm.event.dto.params.EventParams;
import ru.practicum.ewm.event.dto.params.EventParamsSorted;
import ru.practicum.ewm.event.dto.params.PublicSearchParams;
import ru.practicum.ewm.event.dto.projection.EventInfo;
import ru.practicum.ewm.event.dto.request.CreateEventDto;
import ru.practicum.ewm.event.dto.request.UpdateEventDto;
import ru.practicum.ewm.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.request.dto.ParticipationRequestDto;
import ru.practicum.ewm.request.dto.UpdateRequestStatusDto;

import java.util.List;

public interface EventService {

    EventDto create(CreateEventDto createRequest);

    EventDto update(UpdateEventDto updateRequest);

    EventDto adminUpdate(UpdateEventDto dto);

    EventDtoExtended get(Long id);

    EventDtoExtended get(EventParams params);

    List<EventInfo> get(EventParamsSorted params);

    List<EventDtoExtended> get(AdminSearchParams params);

    List<EventDtoShort> get(PublicSearchParams params);

    List<ParticipationRequestDto> getEventRequests(EventParams params);

    EventRequestStatusUpdateResult updateEventRequestStatus(UpdateRequestStatusDto dto);
}
