package ru.practicum.ewm.analyzer.controller;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.ewm.analyzer.service.RecommendationService;
import ru.practicum.ewm.stats.proto.analyzer.*;

import java.util.List;
import java.util.Map;

@GrpcService
@RequiredArgsConstructor
@Slf4j
public class RecommendationsGrpcController
        extends RecommendationsControllerGrpc.RecommendationsControllerImplBase {

    private final RecommendationService recommendationService;

    @Override
    public void getRecommendationsForUser(UserPredictionsRequestProto request,
                                          StreamObserver<RecommendedEventProto> responseObserver) {
        log.info("GetRecommendationsForUser: userId={}, maxResults={}",
                request.getUserId(), request.getMaxResults());
        try {
            List<Map.Entry<Long, Double>> recs = recommendationService
                    .getRecommendationsForUser(request.getUserId(), request.getMaxResults());
            for (Map.Entry<Long, Double> e : recs) {
                responseObserver.onNext(RecommendedEventProto.newBuilder()
                        .setEventId(e.getKey()).setScore(e.getValue()).build());
            }
            responseObserver.onCompleted();
        } catch (Exception ex) {
            log.error("GetRecommendationsForUser failed", ex);
            responseObserver.onError(ex);
        }
    }

    @Override
    public void getSimilarEvents(SimilarEventsRequestProto request,
                                 StreamObserver<RecommendedEventProto> responseObserver) {
        log.info("GetSimilarEvents: eventId={}, userId={}", request.getEventId(), request.getUserId());
        try {
            List<Map.Entry<Long, Double>> similar = recommendationService
                    .getSimilarEvents(request.getEventId(), request.getUserId(), request.getMaxResults());
            for (Map.Entry<Long, Double> e : similar) {
                responseObserver.onNext(RecommendedEventProto.newBuilder()
                        .setEventId(e.getKey()).setScore(e.getValue()).build());
            }
            responseObserver.onCompleted();
        } catch (Exception ex) {
            log.error("GetSimilarEvents failed", ex);
            responseObserver.onError(ex);
        }
    }

    @Override
    public void getInteractionsCount(InteractionsCountRequestProto request,
                                     StreamObserver<RecommendedEventProto> responseObserver) {
        log.info("GetInteractionsCount: eventIds={}", request.getEventIdList());
        try {
            List<Map.Entry<Long, Double>> counts = recommendationService
                    .getInteractionsCount(request.getEventIdList());
            for (Map.Entry<Long, Double> e : counts) {
                responseObserver.onNext(RecommendedEventProto.newBuilder()
                        .setEventId(e.getKey()).setScore(e.getValue()).build());
            }
            responseObserver.onCompleted();
        } catch (Exception ex) {
            log.error("GetInteractionsCount failed", ex);
            responseObserver.onError(ex);
        }
    }
}