package ru.practicum.ewm.analyzer.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.analyzer.model.UserEventInteraction;

import java.util.List;
import java.util.Set;

public interface UserEventInteractionRepository
        extends JpaRepository<UserEventInteraction, UserEventInteraction.UserEventId> {


    @Query("""
            SELECT u FROM UserEventInteraction u
            WHERE u.id.userId = :userId
            ORDER BY u.weight DESC
            """)
    List<UserEventInteraction> findRecentByUserId(
            @Param("userId") long userId,
            Pageable pageable);


    @Query("""
            SELECT u FROM UserEventInteraction u
            WHERE u.id.userId = :userId
              AND u.id.eventId IN :eventIds
            """)
    List<UserEventInteraction> findByUserIdAndEventIds(
            @Param("userId") long userId,
            @Param("eventIds") List<Long> eventIds);


    @Query("SELECT u.id.eventId FROM UserEventInteraction u WHERE u.id.userId = :userId")
    Set<Long> findEventIdsByUserId(@Param("userId") long userId);


    @Query("""
            SELECT u.id.eventId, SUM(u.weight)
            FROM UserEventInteraction u
            WHERE u.id.eventId IN :eventIds
            GROUP BY u.id.eventId
            """)
    List<Object[]> sumWeightsByEventIds(@Param("eventIds") List<Long> eventIds);
}