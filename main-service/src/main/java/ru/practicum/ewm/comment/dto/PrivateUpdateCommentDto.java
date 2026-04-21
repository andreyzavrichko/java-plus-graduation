package ru.practicum.ewm.comment.dto;

import ru.practicum.ewm.comment.dto.request.UpdateCommentBody;

public record PrivateUpdateCommentDto(
        Long userId,
        Long commentId,
        String text
) {
    public static PrivateUpdateCommentDto of(
            Long userId,
            Long commentId,
            UpdateCommentBody body
    ) {
        return new PrivateUpdateCommentDto(
                userId,
                commentId,
                body.text()
        );
    }
}
