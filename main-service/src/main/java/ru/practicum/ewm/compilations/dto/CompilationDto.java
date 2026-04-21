package ru.practicum.ewm.compilations.dto;

import ru.practicum.ewm.event.dto.EventDtoShortWithoutViews;

import java.util.List;

public record CompilationDto(
        Long id,
        Boolean pinned,
        String title,
        List<EventDtoShortWithoutViews> events
) {
}
