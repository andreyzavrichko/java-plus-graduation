package ru.practicum.ewm.comment.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCommentBody(
        @NotBlank
        @Size(min = 20, max = 2000, message = "Comment must be between 20 and 2000 characters")
        String text
) {
}
