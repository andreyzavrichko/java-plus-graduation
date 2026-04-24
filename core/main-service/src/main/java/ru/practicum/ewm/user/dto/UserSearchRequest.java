package ru.practicum.ewm.user.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;

import java.util.List;

@Builder
public record UserSearchRequest(
        List<Long> ids,

        @PositiveOrZero(message = "from должен быть >= 0")
        int from,

        @Positive(message = "size должен быть > 0")
        int size
) {
}
