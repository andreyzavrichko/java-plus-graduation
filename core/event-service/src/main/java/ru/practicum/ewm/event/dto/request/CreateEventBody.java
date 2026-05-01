package ru.practicum.ewm.event.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import ru.practicum.ewm.sharing.annotation.AtLeastHoursFromNow;

import java.time.LocalDateTime;

import static ru.practicum.ewm.sharing.constants.AppConstants.DATE_TIME_FORMAT;

public record CreateEventBody(
        @NotBlank @Size(min = 20, max = 2000) String annotation,
        @NotNull @Positive Long category,
        @NotBlank @Size(min = 20, max = 7000) String description,
        @JsonFormat(pattern = DATE_TIME_FORMAT) @NotNull @AtLeastHoursFromNow(hours = 2) LocalDateTime eventDate,
        LocationBody location,
        Boolean paid,
        @PositiveOrZero Integer participantLimit,
        Boolean requestModeration,
        @NotBlank @Size(min = 3, max = 120) String title
) {
}
