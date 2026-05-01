package ru.practicum.ewm.event.dto.params;

import org.springframework.data.domain.Pageable;
import ru.practicum.ewm.api.sharing.PageableFactory;
import ru.practicum.ewm.event.model.State;

import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.ewm.sharing.constants.AppConstants.DATE_TIME_FORMATTER;
import static ru.practicum.ewm.sharing.constants.AppConstants.EVENTS_DEFAULT_SORT;

public record AdminSearchParams(
        List<Long> users, List<State> states, List<Long> categories,
        LocalDateTime rangeStart, LocalDateTime rangeEnd, Pageable pageable
) {
    public static AdminSearchParams of(List<Long> users, List<State> states, List<Long> categories,
                                       String rangeStart, String rangeEnd, Integer from, Integer size) {
        return new AdminSearchParams(users, states, categories,
                rangeStart != null && !rangeStart.isBlank() ? LocalDateTime.parse(rangeStart, DATE_TIME_FORMATTER) : null,
                rangeEnd != null && !rangeEnd.isBlank() ? LocalDateTime.parse(rangeEnd, DATE_TIME_FORMATTER) : null,
                PageableFactory.offset(from, size, EVENTS_DEFAULT_SORT));
    }
}
