package ru.practicum.ewm.internal;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.api.dto.EventInternalDto;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.exception.NotFoundException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/internal/events")
@RequiredArgsConstructor
public class InternalEventController {

    private final EventRepository eventRepository;
    private final EventInternalMapper mapper;

    @GetMapping("/{eventId}")
    public EventInternalDto getEvent(@PathVariable Long eventId) {
        return eventRepository.findByIdWithCategory(eventId)
                .map(mapper::toInternalDto)
                .orElseThrow(() -> new NotFoundException("Event %d not found".formatted(eventId)));
    }

    @PostMapping("/batch")
    public Map<Long, EventInternalDto> getEventsBatch(@RequestBody List<Long> ids) {
        return eventRepository.findAllByIdInWithCategory(ids).stream()
                .collect(Collectors.toMap(Event::getId, mapper::toInternalDto));
    }

    @GetMapping("/exists/{eventId}")
    public boolean exists(@PathVariable Long eventId) {
        return eventRepository.existsById(eventId);
    }

}