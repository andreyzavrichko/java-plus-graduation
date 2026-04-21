package ru.practicum.ewm.exception;

import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.ewm.sharing.constants.AppConstants.DATE_TIME_FORMATTER;

public record ApiError(
        String timestamp,
        HttpStatus status,
        String message,
        List<String> errors
) {

    public static ApiError of(HttpStatus status, String message, List<String> errors) {
        return new ApiError(
                LocalDateTime.now().format(DATE_TIME_FORMATTER),
                status,
                message,
                errors
        );
    }
}
