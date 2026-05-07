package ru.practicum.ewm.request.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.request.model.ParticipationRequest;
import ru.practicum.ewm.request.model.RequestStatus;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public interface RequestRepository extends JpaRepository<ParticipationRequest, Long> {

    boolean existsByEventIdAndRequesterId(Long eventId, Long requesterId);

    boolean existsByEventIdAndRequesterIdAndStatus(Long eventId, Long requesterId, RequestStatus status); // добавить

    List<ParticipationRequest> findAllByRequesterId(Long requesterId);

    long countByEventIdAndStatus(Long eventId, RequestStatus status);

    List<ParticipationRequest> findAllByEventId(Long eventId);

    @Query("SELECT r.eventId, COUNT(r) FROM ParticipationRequest r " +
            "WHERE r.eventId IN :ids AND r.status = 'CONFIRMED' GROUP BY r.eventId")
    List<Object[]> countConfirmedGroupByEvent(@Param("ids") List<Long> ids);

    default Map<Long, Long> getConfirmedRequestsCounts(List<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) return Map.of();
        return countConfirmedGroupByEvent(eventIds).stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Long) row[1]));
    }
}