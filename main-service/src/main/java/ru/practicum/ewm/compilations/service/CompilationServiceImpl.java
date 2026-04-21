package ru.practicum.ewm.compilations.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.compilations.dto.CompilationDto;
import ru.practicum.ewm.compilations.dto.NewCompilationDto;
import ru.practicum.ewm.compilations.dto.UpdateCompilationRequest;
import ru.practicum.ewm.compilations.mapper.CompilationMapper;
import ru.practicum.ewm.compilations.model.Compilation;
import ru.practicum.ewm.compilations.repository.CompilationRepository;
import ru.practicum.ewm.event.dto.EventDtoShortWithoutViews;
import ru.practicum.ewm.event.mapper.EventMapper;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.request.repository.RequestRepository;
import ru.practicum.ewm.sharing.BaseService;
import ru.practicum.ewm.sharing.EntityName;
import ru.practicum.ewm.sharing.PageableFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class CompilationServiceImpl extends BaseService implements CompilationService {
    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    private final RequestRepository requestRepository;
    private final CompilationMapper compilationMapper;
    private final EventMapper eventMapper;

    @Override
    @Transactional
    public CompilationDto addCompilation(NewCompilationDto newCompilationDto) {
        Compilation compilation = compilationMapper.toEntity(newCompilationDto);

        if (newCompilationDto.events() != null && !newCompilationDto.events().isEmpty()) {
            compilation.setEvents(eventRepository
                    .findAllByIdIn(newCompilationDto.events()));
        }

        Compilation savedCompilation = compilationRepository.save(compilation);

        List<Long> ids = savedCompilation
                .getEvents()
                .stream()
                .map(Event::getId)
                .toList();

        Map<Long, Long> confirmedRequests = requestRepository.getConfirmedRequestsCounts(ids);

        List<EventDtoShortWithoutViews> eventsWithRequests = savedCompilation.getEvents().stream()
                .map(event -> eventMapper.toDtoShort(
                        event, confirmedRequests
                                .getOrDefault(event.getId(), 0L)))
                .toList();

        return new CompilationDto(savedCompilation.getId(),
                savedCompilation.getPinned(),
                savedCompilation.getTitle(),
                eventsWithRequests);
    }

    @Override
    public CompilationDto getCompilationById(Long compilationId) {
        Compilation compilation = findCompilationOrThrow(compilationId);

        List<EventDtoShortWithoutViews> preparedEvents = prepareEvents(compilation);

        return new CompilationDto(
                compilation.getId(),
                compilation.getPinned(),
                compilation.getTitle(),
                preparedEvents
        );
    }

    @Override
    public List<CompilationDto> getCompilations(Boolean pinned, Integer from, Integer size) {
        Pageable pageable = PageableFactory.offset(from, size, Sort.by("id"));

        List<Compilation> compilations = compilationRepository
                .findAllByPinned(pinned, pageable)
                .getContent();

        List<Long> eventsIds = compilations.stream()
                .flatMap(compilation -> compilation.getEvents().stream())
                .map(Event::getId)
                .distinct()
                .toList();

        Map<Long, Long> confirmedRequests = requestRepository.getConfirmedRequestsCounts(eventsIds);

        return compilations.stream()
                .map(compilation -> new CompilationDto(
                        compilation.getId(),
                        compilation.getPinned(),
                        compilation.getTitle(),
                        compilation.getEvents().stream()
                                .map(event -> eventMapper
                                        .toDtoShort(
                                                event, confirmedRequests
                                                        .getOrDefault(event.getId(), 0L)))
                                .toList()))
                .toList();
    }

    @Override
    @Transactional
    public CompilationDto updateCompilation(Long compId, UpdateCompilationRequest updateCompilation) {
        Compilation compilation = findCompilationOrThrow(compId);

        if (updateCompilation.events() != null) {
            Set<Event> events = updateCompilation.events().stream()
                    .map(id -> {
                        Event event = new Event();
                        event.setId(id);
                        return event;
                    })
                    .collect(Collectors.toSet());
            compilation.setEvents(events);
        }

        if (updateCompilation.pinned() != null) {
            compilation.setPinned(updateCompilation.pinned());
        }

        String title = updateCompilation.title();
        if (title != null && !title.isBlank()) {
            compilation.setTitle(title);
        }

        Compilation updatedCompilation = compilationRepository.save(compilation);

        List<EventDtoShortWithoutViews> preparedEvents = prepareEvents(updatedCompilation);

        return new CompilationDto(
                updatedCompilation.getId(),
                updatedCompilation.getPinned(),
                updatedCompilation.getTitle(),
                preparedEvents
        );
    }

    @Override
    @Transactional
    public void deleteCompilation(Long compilationId) {
        findCompilationOrThrow(compilationId);
        compilationRepository.deleteById(compilationId);
    }

    private Compilation findCompilationOrThrow(Long compilationId) {
        return compilationRepository.findById(compilationId)
                .orElseThrow(() -> throwNotFound(compilationId, EntityName.COMPILATION));
    }

    private List<EventDtoShortWithoutViews> prepareEvents(Compilation compilation) {
        if (compilation.getEvents() == null || compilation.getEvents().isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> ids = compilation.getEvents().stream()
                .map(Event::getId)
                .collect(Collectors.toList());

        Map<Long, Long> confirmedRequests = requestRepository.getConfirmedRequestsCounts(ids);

        return compilation.getEvents().stream()
                .map(event -> eventMapper.toDtoShort(event, confirmedRequests.getOrDefault(event.getId(), 0L)))
                .toList();
    }
}
