package ru.practicum.ewm.request.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.request.model.ParticipationRequest;
import ru.practicum.ewm.request.model.RequestStatus;
import ru.practicum.ewm.user.model.User;

import java.util.List;

@Repository
public interface RequestRepository extends JpaRepository<ParticipationRequest, Long>,
        QuerydslPredicateExecutor<ParticipationRequest>,
        RequestRepositoryCustom {

    boolean existsByEventAndRequester(Event event, User requester);

    List<ParticipationRequest> findAllByRequester(User requester);

    long countByEventIdAndStatus(Long eventId, RequestStatus status);

    Page<ParticipationRequest> findAllByEvent(Event event, Pageable pageable);

}
