package ru.practicum.ewm.comment.dto;

import ru.practicum.ewm.comment.dto.request.CreateCommentBody;

public record CreateCommentDto(
        Long authorId,
        Long eventId,
        String text
) {
    public static CreateCommentDto of(Long authorId, Long eventId, CreateCommentBody body) {
        return new CreateCommentDto(
                authorId,
                eventId,
                body.text());
    }
}
