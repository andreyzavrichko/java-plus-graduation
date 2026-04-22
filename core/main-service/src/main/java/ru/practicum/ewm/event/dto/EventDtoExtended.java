package ru.practicum.ewm.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.event.model.Location;
import ru.practicum.ewm.event.model.State;
import ru.practicum.ewm.user.dto.UserShortDto;

import java.time.LocalDateTime;

import static ru.practicum.ewm.sharing.constants.AppConstants.DATE_TIME_FORMAT;

public record EventDtoExtended(
        String annotation,
        CategoryDto category,
        Long confirmedRequests,

        @JsonFormat(pattern = DATE_TIME_FORMAT)
        LocalDateTime createdOn,

        String description,

        @JsonFormat(pattern = DATE_TIME_FORMAT)
        LocalDateTime eventDate,

        Long id,
        UserShortDto initiator,
        Location location,
        Boolean paid,
        Integer participantLimit,

        @JsonFormat(pattern = DATE_TIME_FORMAT)
        LocalDateTime publishedOn,

        Boolean requestModeration,
        State state,
        String title,
        Long views
) {
}
