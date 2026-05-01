package ru.practicum.ewm.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import ru.practicum.ewm.StatsServerUnavailableException;

import java.util.Collections;
import java.util.List;

@RestControllerAdvice
@Slf4j
public class ExceptionController {

    @ExceptionHandler({
            ConstraintViolationException.class,
            MethodArgumentNotValidException.class,
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class,
            HttpMessageNotReadableException.class
    })
    public ResponseEntity<ApiError> handleSpringValidation(final Exception e) {
        List<String> errors = switch (e) {
            case ConstraintViolationException cve -> cve.getConstraintViolations().stream()
                    .map(ConstraintViolation::getMessage).toList();
            case MethodArgumentNotValidException mnv -> mnv.getBindingResult().getFieldErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage).toList();
            case MethodArgumentTypeMismatchException mtm -> {
                String type = mtm.getRequiredType() != null ? mtm.getRequiredType().getName() : "unknown";
                yield List.of("Parameter '%s' should be of type %s".formatted(mtm.getName(), type));
            }
            case MissingServletRequestParameterException msrp ->
                    List.of("Required parameter '%s' is not present".formatted(msrp.getParameterName()));
            case HttpMessageNotReadableException ignored -> List.of("Invalid request body format");
            default -> List.of(e.getMessage());
        };
        log.info("Validation error: {}", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiError.of(HttpStatus.BAD_REQUEST, "Validation Failed", errors));
    }

    @ExceptionHandler
    public ResponseEntity<ApiError> handleValidation(final ValidationException e) {
        log.info("Validation: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiError.of(HttpStatus.BAD_REQUEST, "Validation Failed",
                        List.of(e.getMessage())));
    }

    @ExceptionHandler
    public ResponseEntity<ApiError> handleAccess(final AccessException e) {
        log.info("Access denied: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiError.of(HttpStatus.FORBIDDEN, e.getMessage(),
                        List.of(e.getMessage())));
    }

    @ExceptionHandler
    public ResponseEntity<ApiError> handleNotFound(final NotFoundException e) {
        log.info("Not found: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiError.of(HttpStatus.NOT_FOUND, e.getMessage(),
                        List.of(e.getMessage())));
    }

    @ExceptionHandler
    public ResponseEntity<ApiError> handleConflict(final ConflictException e) {
        log.info("Conflict: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiError.of(HttpStatus.CONFLICT, e.getMessage(),
                        List.of(e.getMessage())));
    }

    @ExceptionHandler
    public ResponseEntity<ApiError> handleServiceUnavailable(final ServiceUnavailableException e) {
        log.error("Service unavailable: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiError.of(HttpStatus.SERVICE_UNAVAILABLE, e.getMessage(),
                        List.of(e.getMessage())));
    }

    @ExceptionHandler
    public ResponseEntity<ApiError> handleStatsUnavailable(final StatsServerUnavailableException e) {
        log.error("Stats server unavailable: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiError.of(HttpStatus.SERVICE_UNAVAILABLE, e.getMessage(),
                        List.of(e.getMessage())));
    }

    @ExceptionHandler
    public ResponseEntity<ApiError> handleUnexpected(final Exception e) {
        log.error("Unexpected exception: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiError.of(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(),
                        Collections.singletonList(e.getMessage())));
    }
}