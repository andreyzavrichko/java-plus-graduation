package ru.practicum.ewm.extra.compilation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.api.dto.EventInternalDto;
import ru.practicum.ewm.api.dto.UserInternalDto;
import ru.practicum.ewm.api.sharing.PageableFactory;
import ru.practicum.ewm.extra.client.EventFeignClient;
import ru.practicum.ewm.extra.client.RequestFeignClient;
import ru.practicum.ewm.extra.client.UserFeignClient;
import ru.practicum.ewm.extra.compilation.dto.*;
import ru.practicum.ewm.extra.compilation.model.Compilation;
import ru.practicum.ewm.extra.compilation.repository.CompilationRepository;
import ru.practicum.ewm.extra.exception.NotFoundException;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    private final EventFeignClient eventFeignClient;
    private final RequestFeignClient requestFeignClient;
    private final UserFeignClient userFeignClient;

    @Override
    @Transactional
    public CompilationDto addCompilation(NewCompilationDto dto) {
        Compilation compilation = new Compilation();
        compilation.setTitle(dto.title());
        compilation.setPinned(dto.pinned());

        if (dto.events() != null && !dto.events().isEmpty()) {
            compilation.setEventIds(new HashSet<>(dto.events()));
        }

        Compilation saved = compilationRepository.save(compilation);
        return buildDto(saved);
    }

    @Override
    public CompilationDto getCompilationById(Long compId) {
        return buildDto(findOrThrow(compId));
    }

    @Override
    public List<CompilationDto> getCompilations(Boolean pinned, Integer from, Integer size) {
        Pageable pageable = PageableFactory.offset(from, size, Sort.by("id"));
        List<Compilation> compilations = compilationRepository.findAllByPinned(pinned, pageable).getContent();
        if (compilations.isEmpty()) return List.of();

        List<Long> allEventIds = compilations.stream()
                .flatMap(c -> c.getEventIds().stream())
                .distinct().toList();

        Map<Long, EventInternalDto> eventsMap = fetchEvents(allEventIds);
        Map<Long, Long> countsMap = fetchCounts(allEventIds);
        Map<Long, UserInternalDto> usersMap = fetchUsers(eventsMap);

        return compilations.stream()
                .map(c -> toDto(c, eventsMap, countsMap, usersMap))
                .toList();
    }

    @Override
    @Transactional
    public CompilationDto updateCompilation(Long compId, UpdateCompilationRequest dto) {
        Compilation compilation = findOrThrow(compId);

        if (dto.events() != null) {
            compilation.setEventIds(new HashSet<>(dto.events()));
        }
        if (dto.pinned() != null) {
            compilation.setPinned(dto.pinned());
        }
        if (dto.title() != null && !dto.title().isBlank()) {
            compilation.setTitle(dto.title());
        }

        Compilation saved = compilationRepository.save(compilation);
        if (saved.getEventIds().isEmpty()) {
            return new CompilationDto(saved.getId(), saved.getPinned(), saved.getTitle(), List.of());
        }
        return buildDto(saved);
    }

    @Override
    @Transactional
    public void deleteCompilation(Long compId) {
        findOrThrow(compId);
        compilationRepository.deleteById(compId);
    }


    private CompilationDto buildDto(Compilation compilation) {
        List<Long> ids = new ArrayList<>(compilation.getEventIds());
        Map<Long, EventInternalDto> eventsMap = fetchEvents(ids);
        Map<Long, Long> countsMap = fetchCounts(ids);
        Map<Long, UserInternalDto> usersMap = fetchUsers(eventsMap);
        return toDto(compilation, eventsMap, countsMap, usersMap);
    }

    private CompilationDto toDto(Compilation c,
                                 Map<Long, EventInternalDto> eventsMap,
                                 Map<Long, Long> countsMap,
                                 Map<Long, UserInternalDto> usersMap) {
        List<EventShortDto> events = c.getEventIds().stream()
                .map(eventsMap::get)
                .filter(Objects::nonNull)
                .map(e -> toEventShort(e, countsMap, usersMap))
                .toList();
        return new CompilationDto(c.getId(), c.getPinned(), c.getTitle(), events);
    }

    private EventShortDto toEventShort(EventInternalDto e,
                                       Map<Long, Long> countsMap,
                                       Map<Long, UserInternalDto> usersMap) {
        UserInternalDto user = usersMap.get(e.initiatorId());
        UserShortDto initiator = user != null
                ? new UserShortDto(user.id(), user.name()) : null;

        return new EventShortDto(
                e.id(),
                e.annotation(),
                new CategoryShortDto(e.categoryId(), e.categoryName()),
                countsMap.getOrDefault(e.id(), 0L),
                e.eventDate(),
                initiator,
                e.paid(),
                e.title()
        );
    }

    private Map<Long, EventInternalDto> fetchEvents(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return Map.of();
        return eventFeignClient.getEventsBatch(ids);
    }

    private Map<Long, Long> fetchCounts(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return Map.of();
        return requestFeignClient.confirmedCounts(ids);
    }

    private Map<Long, UserInternalDto> fetchUsers(Map<Long, EventInternalDto> eventsMap) {
        if (eventsMap.isEmpty()) return Map.of();
        List<Long> initiatorIds = eventsMap.values().stream()
                .map(EventInternalDto::initiatorId)
                .distinct().toList();
        return userFeignClient.getUsersBatch(initiatorIds);
    }

    private Compilation findOrThrow(Long id) {
        return compilationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Compilation %d not found".formatted(id)));
    }
}
