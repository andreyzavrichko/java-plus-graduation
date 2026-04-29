package ru.practicum.ewm.event.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.event.model.Event;

import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long>,
        QuerydslPredicateExecutor<Event> {

    <T> Page<T> findByInitiatorId(Long initiatorId, Pageable pageable, Class<T> type);

    boolean existsByCategoryId(Long categoryId);


    @Query("SELECT e FROM Event e JOIN FETCH e.category WHERE e.id IN :ids")
    List<Event> findAllByIdInWithCategory(@Param("ids") List<Long> ids);

    @Query("SELECT e FROM Event e JOIN FETCH e.category WHERE e.id = :id")
    Optional<Event> findByIdWithCategory(@Param("id") Long id);
}
