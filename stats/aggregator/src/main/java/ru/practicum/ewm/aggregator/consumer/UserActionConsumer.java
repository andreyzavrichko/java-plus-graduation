package ru.practicum.ewm.aggregator.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.aggregator.service.SimilarityService;
import ru.practicum.ewm.stats.avro.UserActionAvro;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserActionConsumer {

    private final SimilarityService similarityService;

    @KafkaListener(
            topics    = "${ewm.kafka.topics.user-actions}",
            groupId   = "aggregator-group",
            containerFactory = "userActionListenerFactory"
    )
    public void consume(UserActionAvro action) {
        log.debug("Consumed action: userId={}, eventId={}, type={}",
                action.getUserId(), action.getEventId(), action.getActionType());
        similarityService.processAction(action);
    }
}