package ru.practicum.ewm.event.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import ru.practicum.ewm.event.model.Event;

import java.util.List;
import java.util.Set;

public interface EventRepository extends JpaRepository<Event, Long>,
        QuerydslPredicateExecutor<Event> {

    <T> Page<T> findByInitiatorId(Long initiatorId, Pageable pageable, Class<T> type);

    boolean existsByCategoryId(Long categoryId);

    Set<Event> findAllByIdIn(List<Long> events);
}
