package ru.practicum.ewm.exception;

public class ServiceUnavailableException extends RuntimeException {
    public ServiceUnavailableException(String m) {
        super(m);
    }
}
