package ru.practicum.ewm.event.service;

import com.querydsl.core.types.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.CreateHitDto;
import ru.practicum.ewm.StatsClient;
import ru.practicum.ewm.ViewStatsDto;
import ru.practicum.ewm.api.dto.UserInternalDto;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.repository.CategoryRepository;
import ru.practicum.ewm.client.RequestFeignClient;
import ru.practicum.ewm.client.UserFeignClient;
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
import ru.practicum.ewm.event.mapper.EventMapper;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.Sort;
import ru.practicum.ewm.event.model.State;
import ru.practicum.ewm.event.model.StateAction;
import ru.practicum.ewm.event.repository.EventPredicateBuilder;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.exception.AccessException;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.request.dto.ParticipationRequestDto;

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
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final UserFeignClient userFeignClient;
    private final RequestFeignClient requestFeignClient;
    private final EventMapper mapper;
    private final StatsClient statsClient;

    @Override
    @Transactional
    public EventDto create(CreateEventDto dto) {
        if (!userFeignClient.exists(dto.userId()))
            throw new NotFoundException("User %d not found".formatted(dto.userId()));
        Category category = findCategoryOrThrow(dto.category());
        Event event = mapper.toEntity(dto);
        event.setCategory(category);
        event.setInitiatorId(dto.userId());
        Event saved = eventRepository.save(event);
        return mapper.toDto(saved, mapper.toUserShort(userFeignClient.getUser(dto.userId())));
    }

    @Override
    @Transactional
    public EventDto update(UpdateEventDto dto) {
        if (!userFeignClient.exists(dto.userId()))
            throw new NotFoundException("User %d not found".formatted(dto.userId()));
        Event event = findEventOrThrow(dto.eventId());
        if (!event.getInitiatorId().equals(dto.userId()))
            throw new AccessException("Only initiator can update event");
        if (event.getState() == State.PUBLISHED)
            throw new ConflictException("Only pending or canceled events can be changed");
        mapper.updateEntity(dto, event);
        if (categoryChanged(event, dto)) event.setCategory(findCategoryOrThrow(dto.category()));
        if (dto.hasStateAction()) applyStateActionPrivate(event, dto.stateAction());
        Event updated = eventRepository.save(event);
        return mapper.toDto(updated, mapper.toUserShort(userFeignClient.getUser(updated.getInitiatorId())));
    }

    @Override
    @Transactional
    public EventDto adminUpdate(UpdateEventDto dto) {
        Event event = findEventOrThrow(dto.eventId());
        mapper.updateEntity(dto, event);
        if (dto.hasStateAction()) {
            if ((dto.stateAction() == StateAction.PUBLISH_EVENT || dto.stateAction() == StateAction.REJECT_EVENT)
                    && event.getState() != State.PENDING)
                throw new ConflictException("Cannot publish/reject: state=%s".formatted(event.getState()));
            applyStateActionAdmin(event, dto.stateAction());
        }
        if (categoryChanged(event, dto)) event.setCategory(findCategoryOrThrow(dto.category()));
        Event updated = eventRepository.save(event);
        return mapper.toDto(updated, mapper.toUserShort(userFeignClient.getUser(updated.getInitiatorId())));
    }

    @Override
    public EventDtoExtended get(Long id) {
        Event event = findEventOrThrow(id);
        if (event.getState() != State.PUBLISHED)
            throw new NotFoundException("Event %d not found".formatted(id));
        createHit(EVENT_PATH_TEMPLATE + id);
        Long views = getStat(List.of(event)).getOrDefault(id, 0L);
        Long confirmed = requestFeignClient.confirmedCounts(List.of(id)).getOrDefault(id, 0L);
        return mapper.toExtendedDto(event, mapper.toUserShort(userFeignClient.getUser(event.getInitiatorId())), views, confirmed);
    }

    @Override
    public List<EventDtoShortWithoutViews> get(EventParamsSorted params) {
        if (!userFeignClient.exists(params.userId()))
            throw new NotFoundException("User %d not found".formatted(params.userId()));

        List<Event> events = eventRepository
                .findByInitiatorId(params.userId(), params.pageable(), Event.class)
                .getContent();

        if (events.isEmpty()) return List.of();

        List<Long> ids = events.stream().map(Event::getId).toList();
        Map<Long, Long> confirmed = requestFeignClient.confirmedCounts(ids);

        List<Long> initiatorIds = events.stream().map(Event::getInitiatorId).distinct().toList();
        Map<Long, UserInternalDto> users = userFeignClient.getUsersBatch(initiatorIds);

        return events.stream()
                .map(e -> mapper.toDtoShortWithoutViews(
                        e,
                        mapper.toUserShort(users.get(e.getInitiatorId())),
                        confirmed.getOrDefault(e.getId(), 0L)))
                .toList();
    }

    @Override
    public EventDtoExtended get(EventParams params) {
        if (!userFeignClient.exists(params.userId()))
            throw new NotFoundException("User %d not found".formatted(params.userId()));
        Event event = findEventOrThrow(params.eventId());
        Long views = getStat(List.of(event)).getOrDefault(event.getId(), 0L);
        Long confirmed = requestFeignClient.confirmedCounts(List.of(params.eventId())).getOrDefault(params.eventId(), 0L);
        return mapper.toExtendedDto(event, mapper.toUserShort(userFeignClient.getUser(event.getInitiatorId())), views, confirmed);
    }

    @Override
    public List<EventDtoExtended> get(AdminSearchParams params) {
        Predicate predicate = new EventPredicateBuilder()
                .withInitiators(params.users()).withStates(params.states())
                .withCategories(params.categories()).withDateRange(params.rangeStart(), params.rangeEnd()).build();
        List<Event> events = eventRepository.findAll(predicate, params.pageable()).getContent();
        if (events.isEmpty()) return List.of();
        List<Long> ids = events.stream().map(Event::getId).toList();
        Map<Long, Long> confirmed = requestFeignClient.confirmedCounts(ids);
        Map<Long, Long> views = getStat(events);
        List<Long> initiatorIds = events.stream().map(Event::getInitiatorId).distinct().toList();
        Map<Long, UserInternalDto> users = userFeignClient.getUsersBatch(initiatorIds);
        return events.stream().map(e -> mapper.toExtendedDto(e,
                mapper.toUserShort(users.get(e.getInitiatorId())),
                views.getOrDefault(e.getId(), 0L),
                confirmed.getOrDefault(e.getId(), 0L))).toList();
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

        List<Event> events = new ArrayList<>(
                eventRepository.findAll(predicate, params.pageable()).getContent()
        );

        createHit(EVENTS_BASE_PATH);

        if (events.isEmpty()) return List.of();

        List<Long> ids = events.stream().map(Event::getId).toList();
        Map<Long, Long> confirmed = requestFeignClient.confirmedCounts(ids);

        if (Boolean.TRUE.equals(params.onlyAvailable())) {
            events = events.stream()
                    .filter(e -> e.getParticipantLimit() == 0 ||
                            confirmed.getOrDefault(e.getId(), 0L) < e.getParticipantLimit())
                    .collect(Collectors.toCollection(ArrayList::new));
        }

        Map<Long, Long> views = getStat(events);

        if (params.sort() == Sort.VIEWS) {
            events.sort(Comparator.comparing(
                    e -> views.getOrDefault(e.getId(), 0L),
                    Comparator.reverseOrder()));
        }

        List<Long> initiatorIds = events.stream().map(Event::getInitiatorId).distinct().toList();
        Map<Long, UserInternalDto> users = userFeignClient.getUsersBatch(initiatorIds);

        return events.stream()
                .map(e -> mapper.toDtoShort(
                        e,
                        mapper.toUserShort(users.get(e.getInitiatorId())),
                        views.getOrDefault(e.getId(), 0L),
                        confirmed.getOrDefault(e.getId(), 0L)))
                .toList();
    }

    @Override
    public List<ParticipationRequestDto> getEventRequests(EventParams params) {
        Event event = findEventOrThrow(params.eventId());
        if (!event.getInitiatorId().equals(params.userId()))
            throw new AccessException("User %d is not initiator of event %d"
                    .formatted(params.userId(), params.eventId()));
        return requestFeignClient.getByEvent(params.eventId());
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult updateEventRequestStatus(
            Long userId, Long eventId, EventRequestStatusUpdateRequest body) {
        Event event = findEventOrThrow(eventId);
        if (!event.getInitiatorId().equals(userId))
            throw new AccessException("User %d is not initiator of event %d".formatted(userId, eventId));

        return requestFeignClient.updateStatuses(userId, eventId, event.getParticipantLimit(), body);
    }

    private Event findEventOrThrow(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Event %d not found".formatted(id)));
    }

    private Category findCategoryOrThrow(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category %d not found".formatted(id)));
    }

    private boolean categoryChanged(Event event, UpdateEventDto dto) {
        return dto.category() != null && !event.getCategory().getId().equals(dto.category());
    }

    private Map<Long, Long> getStat(List<Event> events) {
        if (events.isEmpty()) return Collections.emptyMap();
        List<String> uris = events.stream().map(e -> EVENT_PATH_TEMPLATE + e.getId()).toList();
        LocalDateTime minDate = events.stream().map(Event::getPublishedOn).filter(Objects::nonNull)
                .min(Comparator.naturalOrder()).orElse(LocalDateTime.now());
        List<ViewStatsDto> stats = statsClient.getStats(
                minDate.format(DATE_TIME_FORMATTER), LocalDateTime.now().format(DATE_TIME_FORMATTER),
                uris, true).getBody();
        if (stats == null) return Collections.emptyMap();
        return stats.stream().collect(Collectors.toMap(
                v -> Long.parseLong(v.uri().substring(v.uri().lastIndexOf('/') + 1)),
                ViewStatsDto::hits));
    }

    private void createHit(String uri) {
        try {
            statsClient.createHit(new CreateHitDto(MAIN_APP_NAME, uri,
                    InetAddress.getLocalHost().getHostAddress(), LocalDateTime.now()));
        } catch (UnknownHostException e) {
            throw new RuntimeException("Error creating hit", e);
        }
    }

    private void applyStateActionPrivate(Event event, StateAction action) {
        switch (action) {
            case SEND_TO_REVIEW -> event.setState(State.PENDING);
            case CANCEL_REVIEW -> event.setState(State.CANCELED);
            default -> throw new IllegalArgumentException("Illegal private action: " + action);
        }
    }

    private void applyStateActionAdmin(Event event, StateAction action) {
        switch (action) {
            case PUBLISH_EVENT -> {
                event.setState(State.PUBLISHED);
                event.setPublishedOn(LocalDateTime.now());
            }
            case REJECT_EVENT -> event.setState(State.CANCELED);
            default -> throw new IllegalArgumentException("Illegal admin action: " + action);
        }
    }
}
