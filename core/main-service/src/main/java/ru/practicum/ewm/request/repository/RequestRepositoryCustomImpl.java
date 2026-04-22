package ru.practicum.ewm.request.repository;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.practicum.ewm.request.model.QParticipationRequest;
import ru.practicum.ewm.request.model.RequestStatus;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class RequestRepositoryCustomImpl implements RequestRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public Map<Long, Long> getConfirmedRequestsCounts(List<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return Map.of();
        }

        QParticipationRequest request = QParticipationRequest.participationRequest;

        List<Tuple> results = queryFactory
                .select(request.event.id, request.count())
                .from(request)
                .where(
                        request.event.id.in(eventIds)
                                .and(request.status.eq(RequestStatus.CONFIRMED))
                )
                .groupBy(request.event.id)
                .fetch();

        return results.stream()
                .collect(Collectors.toMap(
                        tuple -> tuple.get(request.event.id),
                        tuple -> Optional.ofNullable(tuple.get(request.count()))
                                .orElse(0L)
                ));
    }
}
