package ru.practicum.ewm.event.model;

public enum State {
    PUBLISHED,
    PENDING,
    CANCELED;

    public static final String DEFAULT_STATE = "PENDING";
}
