package ru.practicum.ewm.event.dto.request;

import java.time.LocalDateTime;

public record CreateEventDto(
        Long userId,
        String annotation,
        Long category,
        String description,
        LocalDateTime eventDate,
        LocationBody location,
        Boolean paid,
        Integer participantLimit,
        Boolean requestModeration,
        String title
) {
    public static CreateEventDto of(CreateEventBody body, Long userId) {
        return new CreateEventDto(
                userId,
                body.annotation(),
                body.category(),
                body.description(),
                body.eventDate(),
                body.location(),
                body.paid(),
                body.participantLimit(),
                body.requestModeration(),
                body.title()
        );
    }
}
