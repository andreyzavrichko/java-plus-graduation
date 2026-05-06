package ru.practicum.ewm.analyzer.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.analyzer.model.EventSimilarity;

import java.util.List;

public interface EventSimilarityRepository
        extends JpaRepository<EventSimilarity, EventSimilarity.EventSimilarityId> {


    @Query("""
            SELECT s FROM EventSimilarity s
            WHERE s.id.eventA = :eventId OR s.id.eventB = :eventId
            ORDER BY s.score DESC
            """)
    List<EventSimilarity> findByEventIdOrderByScoreDesc(
            @Param("eventId") long eventId,
            Pageable pageable);


    @Query("""
            SELECT s FROM EventSimilarity s
            WHERE (s.id.eventA = :targetId AND s.id.eventB IN :visitedIds)
               OR (s.id.eventB = :targetId AND s.id.eventA IN :visitedIds)
            ORDER BY s.score DESC
            """)
    List<EventSimilarity> findNeighbors(
            @Param("targetId") long targetId,
            @Param("visitedIds") List<Long> visitedIds,
            Pageable pageable);
}