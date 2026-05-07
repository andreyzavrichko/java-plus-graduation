package ru.practicum.ewm.aggregator.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;


@Slf4j
@Service
public class SimilarityService {

    private final Map<Long, Map<Long, Double>> eventUserWeights = new HashMap<>();
    private final Map<Long, Double> eventWeightSums = new HashMap<>();
    private final Map<Long, Map<Long, Double>> minWeightSums = new HashMap<>();
    private final KafkaTemplate<Long, EventSimilarityAvro> kafkaTemplate;

    @Value("${ewm.kafka.topics.events-similarity}")
    private String similarityTopic;


    private static final Map<ActionTypeAvro, Double> WEIGHTS = Map.of(
            ActionTypeAvro.VIEW, 0.4,
            ActionTypeAvro.REGISTER, 0.8,
            ActionTypeAvro.LIKE, 1.0
    );

    public SimilarityService(KafkaTemplate<Long, EventSimilarityAvro> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }


    public void processAction(UserActionAvro action) {
        long userId = action.getUserId();
        long eventId = action.getEventId();
        double newWeight = WEIGHTS.getOrDefault(action.getActionType(), 0.0);


        double oldWeight = eventUserWeights
                .computeIfAbsent(eventId, e -> new HashMap<>())
                .getOrDefault(userId, 0.0);


        if (newWeight <= oldWeight) {
            log.debug("Action ignored: userId={}, eventId={}, weight={} <= {}",
                    userId, eventId, newWeight, oldWeight);
            return;
        }


        eventUserWeights.get(eventId).put(userId, newWeight);

        double delta = newWeight - oldWeight;

        double newSa = eventWeightSums.merge(eventId, delta, Double::sum);

        Set<Long> otherEvents = eventUserWeights.keySet();
        for (long otherId : otherEvents) {
            if (otherId == eventId) continue;

            double userWeightOther = eventUserWeights
                    .getOrDefault(otherId, Map.of())
                    .getOrDefault(userId, 0.0);

            if (userWeightOther == 0.0) {
                continue;
            }

            double oldMinContrib = Math.min(oldWeight, userWeightOther);
            double newMinContrib = Math.min(newWeight, userWeightOther);
            double minDelta = newMinContrib - oldMinContrib;

            double newSmin = getMinWeightSum(eventId, otherId) + minDelta;
            putMinWeightSum(eventId, otherId, newSmin);

            double sOther = eventWeightSums.getOrDefault(otherId, 0.0);

            if (newSa <= 0 || sOther <= 0) continue;

            double similarity = newSmin / (Math.sqrt(newSa) * Math.sqrt(sOther));

            long eventA = Math.min(eventId, otherId);
            long eventB = Math.max(eventId, otherId);

            EventSimilarityAvro avro = EventSimilarityAvro.newBuilder()
                    .setEventA(eventA)
                    .setEventB(eventB)
                    .setScore(similarity)
                    .setTimestamp(action.getTimestamp())
                    .build();

            kafkaTemplate.send(similarityTopic, eventA, avro);
            log.debug("Similarity sent: ({},{}) = {}", eventA, eventB, similarity);
        }
    }

    private double getMinWeightSum(long a, long b) {
        long first = Math.min(a, b);
        long second = Math.max(a, b);
        return minWeightSums
                .computeIfAbsent(first, k -> new HashMap<>())
                .getOrDefault(second, 0.0);
    }

    private void putMinWeightSum(long a, long b, double value) {
        long first = Math.min(a, b);
        long second = Math.max(a, b);
        minWeightSums
                .computeIfAbsent(first, k -> new HashMap<>())
                .put(second, value);
    }
}