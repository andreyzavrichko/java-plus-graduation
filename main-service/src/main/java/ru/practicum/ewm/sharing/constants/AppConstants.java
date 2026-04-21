package ru.practicum.ewm.sharing.constants;

import lombok.experimental.UtilityClass;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.practicum.ewm.sharing.PageableFactory;

import java.time.format.DateTimeFormatter;

@UtilityClass
public class AppConstants {

    public static final String MAIN_APP_NAME = "ewm-main-service";

    public static final String EVENT_PATH_TEMPLATE = "/events/";
    public static final String EVENTS_BASE_PATH = "/events";

    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);

    public static final Sort EVENTS_DEFAULT_SORT = Sort.by(Sort.Direction.DESC, "eventDate");

    public static final Pageable REQUESTS_DEFAULT_PAGEABLE = PageableFactory.offset(0, 10, Sort.by("id"));
}
