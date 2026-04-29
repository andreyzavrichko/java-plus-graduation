package ru.practicum.ewm.api.dto;

import ru.practicum.ewm.api.dto.enums.EventStateInternal;

import java.time.LocalDateTime;

public record EventInternalDto(
        Long id,
        String title,
        String annotation,
        Long initiatorId,
        Long categoryId,
        String categoryName,
        EventStateInternal state,
        Boolean requestModeration,
        Integer participantLimit,
        Boolean paid,
        LocalDateTime eventDate,
        LocalDateTime publishedOn
) {
}