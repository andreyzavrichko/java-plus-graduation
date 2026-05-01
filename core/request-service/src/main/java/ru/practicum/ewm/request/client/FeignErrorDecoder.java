package ru.practicum.ewm.request.client;

import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.request.exception.*;

@Component
public class FeignErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder defaultDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        return switch (response.status()) {
            case 404 -> new NotFoundException("Not found: " + methodKey);
            case 409 -> new ConflictException("Conflict: " + methodKey);
            case 403 -> new AccessException("Access denied: " + methodKey);
            default  -> defaultDecoder.decode(methodKey, response);
        };
    }
}