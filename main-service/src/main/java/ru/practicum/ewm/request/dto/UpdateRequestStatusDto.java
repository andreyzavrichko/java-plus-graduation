package ru.practicum.ewm.request.dto;

import ru.practicum.ewm.request.model.RequestStatus;

import java.util.List;

public record UpdateRequestStatusDto(
        List<Long> requestIds,
        RequestStatus status,
        Long userId,
        Long eventId
) {
    public static UpdateRequestStatusDto of(
            UpdateRequestStatusBody body,
            Long userId,
            Long eventId
    ) {
        return new UpdateRequestStatusDto(
                body.requestIds(),
                body.status(),
                userId,
                eventId
        );
    }
}
