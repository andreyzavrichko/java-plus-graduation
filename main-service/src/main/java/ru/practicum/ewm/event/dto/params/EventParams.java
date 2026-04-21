package ru.practicum.ewm.event.dto.params;

public record EventParams(
        Long userId,
        Long eventId
) {
    public static EventParams of(Long userId, Long eventId) {
        return new EventParams(userId, eventId);
    }
}
