package ru.practicum.ewm.client;

import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.exception.AccessException;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.exception.ValidationException;

@Component
public class FeignErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder defaultDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        return switch (response.status()) {
            case 400 -> new ValidationException("Bad request: " + methodKey);
            case 403 -> new AccessException("Access denied: " + methodKey);
            case 404 -> new NotFoundException("Not found: " + methodKey);
            case 409 -> new ConflictException("Conflict: " + methodKey);
            default -> defaultDecoder.decode(methodKey, response);
        };
    }
}