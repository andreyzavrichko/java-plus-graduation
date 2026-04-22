package ru.practicum.ewm;

public class StatsServerUnavailableException extends RuntimeException {

    public StatsServerUnavailableException(String message) {
        super(message);
    }

    public StatsServerUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}