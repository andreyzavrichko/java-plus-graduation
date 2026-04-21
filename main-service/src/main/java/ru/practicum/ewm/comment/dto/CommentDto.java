package ru.practicum.ewm.comment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import ru.practicum.ewm.user.dto.UserShortDto;

import java.time.LocalDateTime;

import static ru.practicum.ewm.sharing.constants.AppConstants.DATE_TIME_FORMAT;

public record CommentDto(
        Long id,
        String text,
        UserShortDto author,

        @JsonFormat(pattern = DATE_TIME_FORMAT)
        LocalDateTime createdOn,
        Long eventId
) {
}
