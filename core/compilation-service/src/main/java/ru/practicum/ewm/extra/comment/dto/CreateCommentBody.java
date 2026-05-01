package ru.practicum.ewm.extra.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCommentBody(
        @NotBlank @Size(min = 20, max = 2000) String text
) {
}
