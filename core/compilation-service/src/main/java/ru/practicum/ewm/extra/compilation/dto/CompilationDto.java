package ru.practicum.ewm.extra.compilation.dto;

import java.util.List;

public record CompilationDto(Long id, Boolean pinned, String title, List<EventShortDto> events) {
}
