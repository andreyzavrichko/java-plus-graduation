package ru.practicum.ewm.extra.comment.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewm.extra.comment.model.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    Slice<Comment> findByEventId(Long eventId, Pageable pageable);
}
