package ru.practicum.ewm.analyzer.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.analyzer.service.RecommendationService;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

@Component
@RequiredArgsConstructor
@Slf4j
public class AnalyzerKafkaConsumer {

    private final RecommendationService recommendationService;

    @KafkaListener(
            topics = "${ewm.kafka.topics.user-actions}",
            groupId = "analyzer-actions-group",
            containerFactory = "userActionListenerFactory"
    )
    public void consumeUserAction(UserActionAvro action) {
        log.debug("Analyzer: user action userId={}, eventId={}",
                action.getUserId(), action.getEventId());
        recommendationService.updateInteraction(action);
    }

    @KafkaListener(
            topics = "${ewm.kafka.topics.events-similarity}",
            groupId = "analyzer-similarity-group",
            containerFactory = "eventSimilarityListenerFactory"
    )
    public void consumeSimilarity(EventSimilarityAvro similarity) {
        log.debug("Analyzer: similarity ({},{}) = {}",
                similarity.getEventA(), similarity.getEventB(), similarity.getScore());
        recommendationService.updateSimilarity(similarity);
    }
}