package ru.practicum.ewm.extra.comment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record CommentDto(
        Long id,
        String text,
        AuthorDto author,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime createdOn,
        Long eventId
) {
}
