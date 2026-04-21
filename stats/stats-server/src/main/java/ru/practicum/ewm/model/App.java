package ru.practicum.ewm.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.practicum.ewm.exception.ValidationException;

@Getter
@RequiredArgsConstructor
public enum App {
    EWM_MAIN_SERVICE("ewm-main-service");

    private final String value;

    public static App fromString(String appName) {
        String normalizedAppName = appName.trim()
                .toUpperCase()
                .replace("-", "_");
        try {
            return App.valueOf(normalizedAppName);
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Invalid app name: " + normalizedAppName);
        }
    }
}