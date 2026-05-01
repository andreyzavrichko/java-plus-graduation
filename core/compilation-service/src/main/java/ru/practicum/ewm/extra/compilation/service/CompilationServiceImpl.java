package ru.practicum.ewm.extra.compilation.service;

import lombok.RequiredArgsConstructor;
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
import ru.practicum.ewm.extra.compilation.dto.CompilationDto;
import ru.practicum.ewm.extra.compilation.dto.NewCompilationDto;
import ru.practicum.ewm.extra.compilation.dto.UpdateCompilationRequest;
import ru.practicum.ewm.extra.compilation.mapper.CompilationMapper;
import ru.practicum.ewm.extra.compilation.model.Compilation;
import ru.practicum.ewm.extra.compilation.repository.CompilationRepository;
import ru.practicum.ewm.extra.exception.NotFoundException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    private final EventFeignClient eventFeignClient;
    private final RequestFeignClient requestFeignClient;
    private final UserFeignClient userFeignClient;
    private final CompilationMapper compilationMapper;

    @Override
    @Transactional
    public CompilationDto addCompilation(NewCompilationDto dto) {
        Compilation compilation = new Compilation();
        compilation.setTitle(dto.title());
        compilation.setPinned(dto.pinned());
        if (dto.events() != null && !dto.events().isEmpty()) {
            compilation.setEventIds(new HashSet<>(dto.events()));
        }
        return buildDto(compilationRepository.save(compilation));
    }

    @Override
    public CompilationDto getCompilationById(Long compId) {
        return buildDto(findOrThrow(compId));
    }

    @Override
    public List<CompilationDto> getCompilations(Boolean pinned, Integer from, Integer size) {
        Pageable pageable = PageableFactory.offset(from, size, Sort.by("id"));
        List<Compilation> compilations = compilationRepository
                .findAllByPinned(pinned, pageable).getContent();
        if (compilations.isEmpty()) return List.of();

        List<Long> allEventIds = compilations.stream()
                .flatMap(c -> c.getEventIds().stream())
                .distinct().toList();

        Map<Long, EventInternalDto> eventsMap = fetchEvents(allEventIds);
        Map<Long, Long> countsMap = fetchCounts(allEventIds);
        Map<Long, UserInternalDto> usersMap = fetchUsers(eventsMap);

        return compilations.stream()
                .map(c -> compilationMapper.toDto(c, eventsMap, countsMap, usersMap))
                .toList();
    }

    @Override
    @Transactional
    public CompilationDto updateCompilation(Long compId, UpdateCompilationRequest dto) {
        Compilation compilation = findOrThrow(compId);
        if (dto.events() != null) compilation.setEventIds(new HashSet<>(dto.events()));
        if (dto.pinned() != null) compilation.setPinned(dto.pinned());
        if (dto.title() != null && !dto.title().isBlank()) compilation.setTitle(dto.title());
        return buildDto(compilationRepository.save(compilation));
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
        return compilationMapper.toDto(compilation, eventsMap, countsMap, usersMap);
        // ← toDto/toEventShort/toUserShort из класса УДАЛЕНЫ
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
        List<Long> ids = eventsMap.values().stream()
                .map(EventInternalDto::initiatorId).distinct().toList();
        return userFeignClient.getUsersBatch(ids);
    }

    private Compilation findOrThrow(Long id) {
        return compilationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        "Compilation %d not found".formatted(id)));
    }
}
