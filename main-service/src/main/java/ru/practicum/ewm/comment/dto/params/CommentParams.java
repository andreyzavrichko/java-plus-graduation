package ru.practicum.ewm.comment.dto.params;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.practicum.ewm.sharing.PageableFactory;

public record CommentParams(
        Long eventId,
        Pageable pageable
) {

    public static CommentParams of(Long eventId, Integer from, Integer size) {
        return new CommentParams(
                eventId,
                PageableFactory.offset(from, size, Sort.by("id"))
        );
    }
}
