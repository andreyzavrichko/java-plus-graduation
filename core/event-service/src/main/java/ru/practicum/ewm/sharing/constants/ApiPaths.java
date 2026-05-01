package ru.practicum.ewm.sharing.constants;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class ApiPaths {
    @UtilityClass
    public class Admin {
        public static final String CATEGORIES = "/admin/categories";
        public static final String EVENTS = "/admin/events";
    }

    @UtilityClass
    public class Public {
        public static final String EVENTS = "/events";
        public static final String CATEGORIES = "/categories";
    }

    @UtilityClass
    public class Private {
        public static final String EVENTS = "/users/{userId}/events";
    }
}
