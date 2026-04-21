package ru.practicum.ewm.event.dto.request;

import ru.practicum.ewm.event.service.StateAction;

import java.time.LocalDateTime;

public record UpdateEventDto(
        String annotation,
        Long category,
        String description,
        LocalDateTime eventDate,
        LocationBody location,
        Boolean paid,
        Integer participantLimit,
        Boolean requestModeration,
        StateAction stateAction,
        String title,
        Long userId,
        Long eventId
) {
    public static UpdateEventDto of(UpdateEventBody body, Long userId, Long eventId) {
        return new UpdateEventDto(
                body.annotation(),
                body.category(),
                body.description(),
                body.eventDate(),
                body.location(),
                body.paid(),
                body.participantLimit(),
                body.requestModeration(),
                body.stateAction(),
                body.title(),
                userId,
                eventId
        );
    }

    public static UpdateEventDto of(AdminUpdateEventBody body, Long eventId) {
        return new UpdateEventDto(
                body.annotation(),
                body.category(),
                body.description(),
                body.eventDate(),
                body.location(),
                body.paid(),
                body.participantLimit(),
                body.requestModeration(),
                body.stateAction(),
                body.title(),
                null,
                eventId
        );
    }

    public boolean hasStateAction() {
        return this.stateAction != null;
    }
}