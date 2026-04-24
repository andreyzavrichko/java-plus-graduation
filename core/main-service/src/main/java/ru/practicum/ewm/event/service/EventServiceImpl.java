package ru.practicum.ewm.event.service;

import com.querydsl.core.types.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.CreateHitDto;
import ru.practicum.ewm.StatsClient;
import ru.practicum.ewm.ViewStatsDto;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.repository.CategoryRepository;
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
import ru.practicum.ewm.event.mapper.EventMapper;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.State;
import ru.practicum.ewm.event.repository.EventPredicateBuilder;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.exception.AccessException;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.request.dto.ParticipationRequestDto;
import ru.practicum.ewm.request.dto.UpdateRequestStatusDto;
import ru.practicum.ewm.request.mapper.RequestMapper;
import ru.practicum.ewm.request.model.ParticipationRequest;
import ru.practicum.ewm.request.model.RequestStatus;
import ru.practicum.ewm.request.repository.RequestRepository;
import ru.practicum.ewm.sharing.BaseService;
import ru.practicum.ewm.sharing.EntityName;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repository.UserRepository;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static ru.practicum.ewm.sharing.constants.AppConstants.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class EventServiceImpl extends BaseService implements EventService {
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final RequestRepository requestRepository;

    private final EventMapper mapper;
    private final RequestMapper requestMapper;

    private final StatsClient statsClient;

    @Override
    @Transactional
    public EventDto create(CreateEventDto dto) {
        User initiator = findUserOrThrow(dto.userId());
        Category category = findCategoryOrThrow(dto.category());

        Event newEvent = mapper.toEntity(dto);
        newEvent.setCategory(category);
        newEvent.setInitiator(initiator);

        Event savedEvent = eventRepository.save(newEvent);

        return mapper.toDto(savedEvent);
    }

    @Transactional
    @Override
    public EventDto update(UpdateEventDto dto) {

        findUserOrThrow(dto.userId());
        Event event = findEventOrThrow(dto.eventId());

        long initiatorId = event.getInitiator().getId();

        if (initiatorId != dto.userId()) {
            throw new AccessException("Only initiator can update event");
        }

        if (event.getState().equals(State.PUBLISHED)) {
            throw new ConflictException("Only pending or canceled events can be changed");
        }

        mapper.updateEntity(dto, event);

        if (categoryChanged(event, dto)) {
            Category category = findCategoryOrThrow(dto.category());
            event.setCategory(category);
        }

        if (dto.hasStateAction()) {
            if (dto.stateAction() == StateAction.PUBLISH_EVENT ||
                (dto.stateAction() == StateAction.REJECT_EVENT)) {
                throw new IllegalArgumentException("Illegal private state action: %s".formatted(dto.stateAction()));
            }
            applyStateAction(event, dto.stateAction());
        }

        Event updatedEvent = eventRepository.save(event);

        return mapper.toDto(updatedEvent);
    }

    @Transactional
    @Override
    public EventDto adminUpdate(UpdateEventDto dto) {
        Event event = findEventOrThrow(dto.eventId());
        mapper.updateEntity(dto, event);

        if (dto.hasStateAction()) {
            if ((dto.stateAction() == StateAction.PUBLISH_EVENT ||
                 dto.stateAction() == StateAction.REJECT_EVENT) &&
                event.getState() != State.PENDING) {

                throw new ConflictException("Cannot publish/reject the event because it's not in the right state: %s"
                        .formatted(event.getState()));

            }
            if (dto.stateAction() == StateAction.SEND_TO_REVIEW ||
                dto.stateAction() == StateAction.CANCEL_REVIEW) {
                throw new IllegalArgumentException("Illegal admin state action: %s".formatted(dto.stateAction()));
            }

            applyStateAction(event, dto.stateAction());
        }

        if (categoryChanged(event, dto)) {
            Category category = findCategoryOrThrow(dto.category());
            event.setCategory(category);
        }

        Event updatedEvent = eventRepository.save(event);

        return mapper.toDto(updatedEvent);
    }

    @Override
    public EventDtoExtended get(Long id) {
        Event event = findEventOrThrow(id);

        if (!event.getState().equals(State.PUBLISHED)) {
            throw new NotFoundException(String.format("Event with Id=%d was not found", id));
        }

        createHit(createUri(id));

        Long views = getStat(List.of(event))
                .getOrDefault(event.getId(), 0L);

        Long confirmedRequests = requestRepository.countByEventIdAndStatus(id, RequestStatus.CONFIRMED);

        return mapper.toExtendedDto(event, views, confirmedRequests);
    }

    @Override
    public List<EventInfo> get(EventParamsSorted params) {
        findUserOrThrow(params.userId());

        return eventRepository.findByInitiatorId(
                        params.userId(),
                        params.pageable(),
                        EventInfo.class)
                .getContent();
    }

    @Override
    public EventDtoExtended get(EventParams params) {
        findUserOrThrow(params.userId());

        Event event = findEventOrThrow(params.eventId());

        Long views = getStat(List.of(event))
                .getOrDefault(params.eventId(), 0L);

        long confirmedRequests = requestRepository
                .countByEventIdAndStatus(params.eventId(), RequestStatus.CONFIRMED);

        return mapper.toExtendedDto(event, views, confirmedRequests);
    }

    @Override
    public List<EventDtoExtended> get(AdminSearchParams params) {
        Predicate predicate = new EventPredicateBuilder()
                .withInitiators(params.users())
                .withStates(params.states())
                .withCategories(params.categories())
                .withDateRange(params.rangeStart(), params.rangeEnd())
                .build();

        List<Event> events = eventRepository.findAll(predicate, params.pageable())
                .getContent();

        if (events.isEmpty()) {
            return List.of();
        }

        List<Long> eventsIds = events.stream()
                .map(Event::getId)
                .toList();

        Map<Long, Long> confirmedRequests = requestRepository.getConfirmedRequestsCounts(eventsIds);

        Map<Long, Long> views = getStat(events);

        return events.stream()
                .map(event -> mapper.toExtendedDto(
                        event,
                        views.getOrDefault(event.getId(), 0L),
                        confirmedRequests.getOrDefault(event.getId(), 0L)))
                .toList();
    }

    @Override
    public List<EventDtoShort> get(PublicSearchParams params) {
        Predicate predicate = new EventPredicateBuilder()
                .withTextSearch(params.text())
                .withCategories(params.categories())
                .withPaid(params.paid())
                .withDateRange(params.rangeStart(), params.rangeEnd())
                .forPublicSearch()
                .build();

        List<Event> events = eventRepository.findAll(predicate, params.pageable())
                .getContent();

        createHit(EVENTS_BASE_PATH);

        if (events.isEmpty()) {
            return List.of();
        }

        List<Long> eventsIds = events.stream()
                .map(Event::getId)
                .toList();

        Map<Long, Long> confirmedRequests = requestRepository.getConfirmedRequestsCounts(eventsIds);

        events = filterAvailableEvents(events, confirmedRequests);

        Map<Long, Long> views = getStat(events);

        if (params.sort().equals(Sort.VIEWS)) {
            events.sort(Comparator.comparing(
                    event -> views.getOrDefault(event.getId(), 0L),
                    Comparator.reverseOrder()
            ));
        }

        return events.stream()
                .map(event -> mapper.toDtoShort(
                        event,
                        views.getOrDefault(event.getId(), 0L),
                        confirmedRequests.getOrDefault(event.getId(), 0L)))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getEventRequests(EventParams params) {
        Event event = findEventOrThrow(params.eventId());
        long initiatorId = event.getInitiator().getId();

        if (initiatorId != params.userId()) {
            throw new AccessException(
                    "User %d attempted to view requests for event %d, but is not the initiator"
                            .formatted(params.userId(), params.eventId()));
        }

        List<ParticipationRequest> result = requestRepository.findAllByEvent(event, REQUESTS_DEFAULT_PAGEABLE)
                .getContent();

        return result.stream()
                .map(requestMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult updateEventRequestStatus(UpdateRequestStatusDto dto) {
        Event event = findEventOrThrow(dto.eventId());

        if (!event.getInitiator().getId().equals(dto.userId())) {
            throw new AccessException(
                    """
                    User %d is not authorized to manage requests for event %d.
                    Only event initiator can perform this action.
                    """.formatted(dto.userId(), dto.eventId())
            );
        }

        if (!event.getRequestModeration()) {
            throw new ConflictException(
                    "Event %d has request moderation disabled. Cannot update request statuses"
                            .formatted(dto.eventId())
            );
        }

        if (event.getState() != State.PUBLISHED) {
            throw new ConflictException(
                    "Cannot update request statuses for event %d: event is not published (current state: %s)"
                            .formatted(dto.eventId(), event.getState())
            );
        }

        long confirmedCount = requestRepository.countByEventIdAndStatus(dto.eventId(), RequestStatus.CONFIRMED);
        int participantLimit = event.getParticipantLimit();

        List<ParticipationRequest> requests = requestRepository.findAllById(dto.requestIds());

        List<ParticipationRequest> toConfirm = new ArrayList<>();
        List<ParticipationRequest> toReject = new ArrayList<>();

        for (ParticipationRequest request : requests) {
            if (request.getStatus() != RequestStatus.PENDING) {
                throw new ConflictException(
                        "Request %d is not in PENDING state (current: %s). Only PENDING requests can be updated"
                                .formatted(request.getId(), request.getStatus())
                );
            }

            if (dto.status() == RequestStatus.CONFIRMED) {
                if (participantLimit > 0 && confirmedCount >= participantLimit) {
                    throw new ConflictException(
                            "Event %d has reached participant limit. Cannot confirm more requests (%d/%d)"
                                    .formatted(dto.eventId(), confirmedCount, participantLimit)
                    );
                }

                request.setStatus(RequestStatus.CONFIRMED);
                toConfirm.add(request);
                confirmedCount++;

            } else if (dto.status() == RequestStatus.REJECTED) {
                request.setStatus(RequestStatus.REJECTED);
                toReject.add(request);
            }
        }

        requestRepository.saveAll(requests);

        List<ParticipationRequestDto> confirmedDtos = toConfirm.stream()
                .map(requestMapper::toDto)
                .toList();

        List<ParticipationRequestDto> rejectedDtos = toReject.stream()
                .map(requestMapper::toDto)
                .toList();

        return new EventRequestStatusUpdateResult(confirmedDtos, rejectedDtos);
    }

    private Event findEventOrThrow(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> throwNotFound(eventId, EntityName.EVENT));
    }

    private User findUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> throwNotFound(userId, EntityName.USER));
    }

    private Category findCategoryOrThrow(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> throwNotFound(categoryId, EntityName.CATEGORY));
    }

    private Map<Long, Long> getStat(List<Event> events) {
        if (events.isEmpty()) {
            return Collections.emptyMap();
        }

        List<String> uris = events.stream()
                .map(p -> EVENT_PATH_TEMPLATE + p.getId())
                .toList();

        LocalDateTime minDataTime = events.stream()
                .map(Event::getPublishedOn)
                .filter(Objects::nonNull)
                .min(Comparator.naturalOrder())
                .orElse(LocalDateTime.now());

        List<ViewStatsDto> stats = statsClient.getStats(
                        minDataTime.format(DATE_TIME_FORMATTER),
                        LocalDateTime.now().format(DATE_TIME_FORMATTER),
                        uris,
                        true)
                .getBody();

        if (stats == null) {
            return Collections.emptyMap();
        }

        return stats.stream().collect(Collectors.toMap(
                view -> extractIdFromUri(view.uri()),
                ViewStatsDto::hits
        ));
    }

    private void createHit(String uri) {
        try {
            CreateHitDto dto = new CreateHitDto(
                    MAIN_APP_NAME,
                    uri,
                    InetAddress.getLocalHost().getHostAddress(),
                    LocalDateTime.now());

            statsClient.createHit(dto);

        } catch (UnknownHostException e) {
            throw new RuntimeException("Error while creating hit.", e);
        }
    }

    private boolean hasValidResponse(ResponseEntity<List<ViewStatsDto>> response) {
        if (response == null) {
            return false;
        }

        if (!response.getStatusCode().is2xxSuccessful()) {
            log.error("Response status code is {}", response.getStatusCode());
            return false;
        }

        List<ViewStatsDto> body = response.getBody();
        return body != null && !body.isEmpty();
    }

    private String createUri(Long eventId) {
        return EVENT_PATH_TEMPLATE + eventId;
    }

    private Long extractIdFromUri(String uri) {
        String[] split = uri.split("/");
        String lastPart = split[split.length - 1];
        return Long.parseLong(lastPart);
    }

    private boolean categoryChanged(Event event, UpdateEventDto dto) {
        Long dtoCategoryId = dto.category();

        if (dtoCategoryId == null) {
            return false;
        }

        Long eventCategoryId = event.getCategory().getId();
        return !eventCategoryId.equals(dtoCategoryId);
    }

    private void applyStateAction(Event event, StateAction stateAction) {
        switch (stateAction) {
            case PUBLISH_EVENT -> {
                event.setState(State.PUBLISHED);
                event.setPublishedOn(LocalDateTime.now());
            }
            case REJECT_EVENT, CANCEL_REVIEW -> event.setState(State.CANCELED);

            case SEND_TO_REVIEW -> event.setState(State.PENDING);
            default -> throw new IllegalArgumentException("Unacceptable state action: " + stateAction);
        }
    }

    private List<Event> filterAvailableEvents(List<Event> events, Map<Long, Long> confirmedRequests) {
        return events.stream()
                .filter(event -> {
                    long requestsCount = confirmedRequests.getOrDefault(event.getId(), 0L);
                    return requestsCount < event.getParticipantLimit();
                })
                .collect(Collectors.toCollection(ArrayList::new));
    }
}