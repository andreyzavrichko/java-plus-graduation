package ru.practicum.ewm.exception;

import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;

public record ApiError(
        LocalDateTime timestamp,
        HttpStatus status,
        String message,
        List<String> errors) {

    public static ApiError of(HttpStatus status, String message, List<String> errors) {
        return new ApiError(
                LocalDateTime.now(),
                status,
                message,
                errors
        );
    }
}
