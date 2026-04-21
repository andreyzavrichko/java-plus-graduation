package ru.practicum.ewm.sharing.constants;

import lombok.experimental.UtilityClass;

/**
 * API path constants for the entire application.
 * Grouped by functional areas.
 */

@UtilityClass
public final class ApiPaths {

    @UtilityClass
    public class Admin {
        public static final String USERS = "/admin/users";
        public static final String CATEGORIES = "/admin/categories";
        public static final String EVENTS = "/admin/events";
        public static final String COMPILATIONS = "/admin/compilations";
        public static final String COMMENTS = "/admin/comments";
    }

    @UtilityClass
    public class Public {
        public static final String COMPILATIONS = "/compilations";
        public static final String EVENTS = "/events";
        public static final String CATEGORIES = "/categories";
        public static final String COMMENTS = "/events/{eventId}/comments";
    }

    @UtilityClass
    public class Private {
        public static final String EVENTS = "/users/{userId}/events";
        public static final String COMMENTS = "/users/{userId}/comments";
        public static final String REQUESTS = "/users/{userId}/requests";
    }
}
