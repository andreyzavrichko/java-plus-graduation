package ru.practicum.ewm;

import java.time.LocalDateTime;

public record HitDto(
        String app,
        String uri,
        String ip,
        LocalDateTime timestamp
) {
}