package ru.practicum.ewm.event.dto.params;

import org.springframework.data.domain.Pageable;
import ru.practicum.ewm.sharing.PageableFactory;

import static ru.practicum.ewm.sharing.constants.AppConstants.EVENTS_DEFAULT_SORT;

public record EventParamsSorted(
        Long userId,
        Pageable pageable
) {
    public static EventParamsSorted of(Long userId, Integer from, Integer size) {
        Pageable pageable = PageableFactory.offset(from, size, EVENTS_DEFAULT_SORT);

        return new EventParamsSorted(
                userId,
                pageable
        );
    }
}
