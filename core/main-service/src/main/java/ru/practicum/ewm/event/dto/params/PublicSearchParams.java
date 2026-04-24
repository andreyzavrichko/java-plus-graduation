package ru.practicum.ewm.event.dto.params;

import org.springframework.data.domain.Pageable;
import ru.practicum.ewm.event.service.Sort;
import ru.practicum.ewm.exception.ValidationException;
import ru.practicum.ewm.sharing.PageableFactory;

import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.ewm.sharing.constants.AppConstants.DATE_TIME_FORMATTER;
import static ru.practicum.ewm.sharing.constants.AppConstants.EVENTS_DEFAULT_SORT;

public record PublicSearchParams(
        String text,
        List<Long> categories,
        Boolean paid,
        LocalDateTime rangeStart,
        LocalDateTime rangeEnd,
        Boolean onlyAvailable,
        Sort sort,
        Pageable pageable
) {
    public static PublicSearchParams of(
            String text,
            List<Long> categories,
            Boolean paid,
            String rangeStart,
            String rangeEnd,
            Boolean onlyAvailable,
            String stringSort,
            Integer from,
            Integer size) {

        if (categories != null && !categories.isEmpty()) {
            for (Long id : categories) {
                if (id == null || id < 1) {
                    throw new ValidationException(
                            String.format("Invalid category commentId: %s. Category commentId must be positive", id));
                }
            }
        }

        Pageable pageable;
        Sort sort;
        LocalDateTime start = null;
        LocalDateTime end = null;

        if (stringSort != null &&
                !stringSort.isBlank() &&
                Sort.valueOf(stringSort) == Sort.EVENT_DATE) {

            sort = Sort.EVENT_DATE;
            pageable = PageableFactory.offset(from, size, EVENTS_DEFAULT_SORT);

        } else {
            sort = Sort.VIEWS;
            pageable = PageableFactory.offset(from, size);
        }

        if (rangeStart != null && !rangeStart.isBlank()) {
            start = LocalDateTime.parse(rangeStart, DATE_TIME_FORMATTER);
        }

        if (rangeEnd != null && !rangeEnd.isBlank()) {
            end = LocalDateTime.parse(rangeEnd, DATE_TIME_FORMATTER);
        }

        return new PublicSearchParams(
                text,
                categories,
                paid,
                start,
                end,
                onlyAvailable,
                sort,
                pageable
        );
    }
}

