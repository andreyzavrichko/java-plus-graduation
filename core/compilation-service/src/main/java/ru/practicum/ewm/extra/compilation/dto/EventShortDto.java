package ru.practicum.ewm.extra.compilation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;


public record EventShortDto(
        Long id,
        String annotation,
        CategoryShortDto category,
        Long confirmedRequests,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime eventDate,
        UserShortDto initiator,
        Boolean paid,
        String title
) {
}
