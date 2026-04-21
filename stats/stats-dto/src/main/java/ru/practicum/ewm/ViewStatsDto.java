package ru.practicum.ewm;

public record ViewStatsDto(
        String app,
        String uri,
        Long hits
) {
}
