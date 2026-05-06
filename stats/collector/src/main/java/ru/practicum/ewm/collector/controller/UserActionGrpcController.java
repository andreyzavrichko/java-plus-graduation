package ru.practicum.ewm.collector.controller;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import ru.practicum.ewm.collector.mapper.ActionMapper;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.ewm.stats.proto.collector.UserActionControllerGrpc;
import ru.practicum.ewm.stats.proto.collector.UserActionProto;

@GrpcService
@RequiredArgsConstructor
@Slf4j
public class UserActionGrpcController
        extends UserActionControllerGrpc.UserActionControllerImplBase {

    private final KafkaTemplate<Long, UserActionAvro> kafkaTemplate;

    @Value("${ewm.kafka.topics.user-actions}")
    private String userActionsTopic;

    @Override
    public void collectUserAction(UserActionProto request,
                                  StreamObserver<Empty> responseObserver) {
        log.info("Received action: userId={}, eventId={}, type={}",
                request.getUserId(), request.getEventId(), request.getActionType());

        UserActionAvro avro = ActionMapper.toAvro(request);
        kafkaTemplate.send(userActionsTopic, avro.getUserId(), avro);

        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }
}