package ru.practicum.ewm.grpc;

import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.proto.analyzer.InteractionsCountRequestProto;
import ru.practicum.ewm.stats.proto.analyzer.RecommendedEventProto;
import ru.practicum.ewm.stats.proto.analyzer.RecommendationsControllerGrpc;
import ru.practicum.ewm.stats.proto.analyzer.SimilarEventsRequestProto;
import ru.practicum.ewm.stats.proto.analyzer.UserPredictionsRequestProto;

import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Component
@Slf4j
public class AnalyzerGrpcClient {

    @GrpcClient("analyzer")
    private RecommendationsControllerGrpc.RecommendationsControllerBlockingStub analyzerStub;

    public Stream<RecommendedEventProto> getRecommendationsForUser(long userId, int maxResults) {
        UserPredictionsRequestProto request = UserPredictionsRequestProto.newBuilder()
                .setUserId(userId)
                .setMaxResults(maxResults)
                .build();
        try {
            return asStream(analyzerStub.getRecommendationsForUser(request));
        } catch (Exception e) {
            log.warn("getRecommendationsForUser failed for userId={}: {}", userId, e.getMessage());
            return Stream.empty();
        }
    }

    public Stream<RecommendedEventProto> getSimilarEvents(long eventId, long userId, int maxResults) {
        SimilarEventsRequestProto request = SimilarEventsRequestProto.newBuilder()
                .setEventId(eventId)
                .setUserId(userId)
                .setMaxResults(maxResults)
                .build();
        try {
            return asStream(analyzerStub.getSimilarEvents(request));
        } catch (Exception e) {
            log.warn("getSimilarEvents failed for eventId={}: {}", eventId, e.getMessage());
            return Stream.empty();
        }
    }

    public Stream<RecommendedEventProto> getInteractionsCount(List<Long> eventIds) {
        InteractionsCountRequestProto request = InteractionsCountRequestProto.newBuilder()
                .addAllEventId(eventIds)
                .build();
        try {
            return asStream(analyzerStub.getInteractionsCount(request));
        } catch (Exception e) {
            log.warn("getInteractionsCount failed: {}", e.getMessage());
            return Stream.empty();
        }
    }

    private Stream<RecommendedEventProto> asStream(Iterator<RecommendedEventProto> iterator) {
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED),
                false
        );
    }
}