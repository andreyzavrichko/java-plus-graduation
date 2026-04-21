package ru.practicum.ewm.event.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import ru.practicum.ewm.event.service.StateAction;
import ru.practicum.ewm.sharing.annotation.AtLeastHoursFromNow;

import java.time.LocalDateTime;

import static ru.practicum.ewm.sharing.constants.AppConstants.DATE_TIME_FORMAT;

public record AdminUpdateEventBody(
        @Size(min = 20, max = 2000, message = "Annotation must be between 20 and 2000 characters")
        String annotation,

        @Positive(message = "Category ID must be > 0")
        Long category,

        @Size(min = 20, max = 7000, message = "Description must be between 20 and 7000 characters")
        String description,

        @JsonFormat(pattern = DATE_TIME_FORMAT)
        @AtLeastHoursFromNow(hours = 1)
        LocalDateTime eventDate,

        LocationBody location,

        Boolean paid,

        @PositiveOrZero(message = "Participant limit cannot be < 0")
        Integer participantLimit,

        Boolean requestModeration,

        StateAction stateAction,

        @Size(min = 3, max = 120, message = "Title must be between 3 and 120 characters")
        String title
) {
}
