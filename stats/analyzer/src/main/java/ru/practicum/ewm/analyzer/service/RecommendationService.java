package ru.practicum.ewm.analyzer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.analyzer.model.EventSimilarity;
import ru.practicum.ewm.analyzer.model.UserEventInteraction;
import ru.practicum.ewm.analyzer.model.UserEventInteraction.UserEventId;
import ru.practicum.ewm.analyzer.repository.EventSimilarityRepository;
import ru.practicum.ewm.analyzer.repository.UserEventInteractionRepository;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class RecommendationService {

    private final EventSimilarityRepository similarityRepo;
    private final UserEventInteractionRepository interactionRepo;

    private static final Map<ActionTypeAvro, Double> WEIGHTS = Map.of(
            ActionTypeAvro.VIEW, 0.4,
            ActionTypeAvro.REGISTER, 0.8,
            ActionTypeAvro.LIKE, 1.0
    );

    @Transactional
    public void updateSimilarity(EventSimilarityAvro avro) {
        long a = Math.min(avro.getEventA(), avro.getEventB());
        long b = Math.max(avro.getEventA(), avro.getEventB());
        log.debug("updateSimilarity: pair=({},{}) score={}", a, b, avro.getScore());

        var id = new EventSimilarity.EventSimilarityId(a, b);
        EventSimilarity entity = similarityRepo.findById(id)
                .orElseGet(() -> {
                    log.debug("updateSimilarity: new pair ({},{})", a, b);
                    return EventSimilarity.builder().id(id).build();
                });
        entity.setScore(avro.getScore());
        similarityRepo.save(entity);
        log.debug("updateSimilarity: saved pair=({},{}) score={}", a, b, avro.getScore());
    }

    @Transactional
    public void updateInteraction(UserActionAvro avro) {
        double newWeight = WEIGHTS.getOrDefault(avro.getActionType(), 0.0);
        var id = new UserEventId(avro.getUserId(), avro.getEventId());
        log.debug("updateInteraction: userId={}, eventId={}, type={}, weight={}",
                avro.getUserId(), avro.getEventId(), avro.getActionType(), newWeight);

        UserEventInteraction entity = interactionRepo.findById(id)
                .orElseGet(() -> {
                    log.debug("updateInteraction: new interaction userId={}, eventId={}",
                            avro.getUserId(), avro.getEventId());
                    return UserEventInteraction.builder().id(id).build();
                });

        if (entity.getWeight() == null || newWeight > entity.getWeight()) {
            log.debug("updateInteraction: weight updated {} -> {} for userId={}, eventId={}",
                    entity.getWeight(), newWeight, avro.getUserId(), avro.getEventId());
            entity.setWeight(newWeight);
            interactionRepo.save(entity);
        } else {
            log.debug("updateInteraction: weight NOT updated (new={} <= current={}) for userId={}, eventId={}",
                    newWeight, entity.getWeight(), avro.getUserId(), avro.getEventId());
        }
    }

    public List<Map.Entry<Long, Double>> getRecommendationsForUser(long userId, int maxResults) {
        log.info("getRecommendationsForUser: userId={}, maxResults={}", userId, maxResults);
        int recentLimit = maxResults * 3;

        List<UserEventInteraction> recent = interactionRepo.findRecentByUserId(
                userId, PageRequest.of(0, recentLimit));
        if (recent.isEmpty()) {
            log.info("getRecommendationsForUser: no interactions for userId={}, returning empty", userId);
            return List.of();
        }
        log.debug("getRecommendationsForUser: userId={} has {} recent interactions", userId, recent.size());

        Set<Long> visitedIds = interactionRepo.findEventIdsByUserId(userId);

        Map<Long, Double> candidates = new LinkedHashMap<>();
        for (UserEventInteraction visited : recent) {
            long visitedEventId = visited.getId().getEventId();
            List<EventSimilarity> similar = similarityRepo.findByEventIdOrderByScoreDesc(
                    visitedEventId, PageRequest.of(0, maxResults * 2));
            for (EventSimilarity sim : similar) {
                long otherId = sim.getId().getEventA() == visitedEventId
                        ? sim.getId().getEventB()
                        : sim.getId().getEventA();
                if (!visitedIds.contains(otherId)) {
                    candidates.merge(otherId, sim.getScore(), Math::max);
                }
            }
        }
        log.debug("getRecommendationsForUser: userId={} found {} candidate events", userId, candidates.size());

        List<Long> topCandidates = candidates.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(maxResults)
                .map(Map.Entry::getKey)
                .toList();

        if (topCandidates.isEmpty()) {
            log.info("getRecommendationsForUser: no candidates for userId={}", userId);
            return List.of();
        }

        Map<Long, Double> visitedWeightMap = recent.stream()
                .collect(Collectors.toMap(
                        i -> i.getId().getEventId(),
                        UserEventInteraction::getWeight));

        List<Map.Entry<Long, Double>> result = new ArrayList<>();
        for (long candidateId : topCandidates) {
            int kNeighbors = Math.min(10, visitedIds.size());
            List<Long> visitedList = new ArrayList<>(visitedIds);
            List<EventSimilarity> neighbors = similarityRepo.findNeighbors(
                    candidateId, visitedList, PageRequest.of(0, kNeighbors));

            double numerator = 0.0;
            double denominator = 0.0;
            for (EventSimilarity n : neighbors) {
                long neighborId = n.getId().getEventA() == candidateId
                        ? n.getId().getEventB()
                        : n.getId().getEventA();
                double simScore = n.getScore();
                double userWeight = visitedWeightMap.getOrDefault(neighborId, 0.0);
                numerator += simScore * userWeight;
                denominator += simScore;
            }

            if (denominator > 0) {
                double score = numerator / denominator;
                log.debug("getRecommendationsForUser: candidateId={} score={}", candidateId, score);
                result.add(Map.entry(candidateId, score));
            } else {
                log.debug("getRecommendationsForUser: candidateId={} skipped (denominator=0)", candidateId);
            }
        }

        result.sort(Map.Entry.<Long, Double>comparingByValue().reversed());
        List<Map.Entry<Long, Double>> finalResult = result.subList(0, Math.min(maxResults, result.size()));
        log.info("getRecommendationsForUser: userId={} returning {} recommendations", userId, finalResult.size());
        return finalResult;
    }

    public List<Map.Entry<Long, Double>> getSimilarEvents(long eventId, long userId, int maxResults) {
        log.info("getSimilarEvents: eventId={}, userId={}, maxResults={}", eventId, userId, maxResults);

        Set<Long> visitedIds = interactionRepo.findEventIdsByUserId(userId);
        log.debug("getSimilarEvents: userId={} visited {} events", userId, visitedIds.size());

        List<EventSimilarity> similar = similarityRepo.findByEventIdOrderByScoreDesc(
                eventId, PageRequest.of(0, maxResults * 3));
        log.debug("getSimilarEvents: eventId={} has {} similar events in DB", eventId, similar.size());

        List<Map.Entry<Long, Double>> result = similar.stream()
                .filter(s -> {
                    long otherId = s.getId().getEventA() == eventId
                            ? s.getId().getEventB()
                            : s.getId().getEventA();
                    return !visitedIds.contains(otherId);
                })
                .limit(maxResults)
                .map(s -> {
                    long otherId = s.getId().getEventA() == eventId
                            ? s.getId().getEventB()
                            : s.getId().getEventA();
                    return Map.entry(otherId, s.getScore());
                })
                .toList();

        log.info("getSimilarEvents: eventId={} returning {} similar events for userId={}",
                eventId, result.size(), userId);
        return result;
    }

    public List<Map.Entry<Long, Double>> getInteractionsCount(List<Long> eventIds) {
        log.info("getInteractionsCount: eventIds={}", eventIds);

        List<Map.Entry<Long, Double>> result = interactionRepo.sumWeightsByEventIds(eventIds).stream()
                .map(row -> Map.entry(((Number) row[0]).longValue(),
                        ((Number) row[1]).doubleValue()))
                .toList();

        log.info("getInteractionsCount: returning {} entries", result.size());
        return result;
    }
}