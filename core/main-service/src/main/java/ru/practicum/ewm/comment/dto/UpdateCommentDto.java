package ru.practicum.ewm.comment.dto;

import ru.practicum.ewm.comment.dto.request.UpdateCommentBody;

public record UpdateCommentDto(
        Long commentId,
        String text
) {
    public static UpdateCommentDto of(Long commentId, UpdateCommentBody body) {
        return new UpdateCommentDto(
                commentId,
                body.text());
    }
}
