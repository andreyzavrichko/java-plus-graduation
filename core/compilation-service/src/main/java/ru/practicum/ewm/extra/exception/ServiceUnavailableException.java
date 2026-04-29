package ru.practicum.ewm.extra.exception;

public class ServiceUnavailableException extends RuntimeException {
    public ServiceUnavailableException(String m) {
        super(m);
    }
}
