package ru.practicum.ewm.grpc;

import com.google.protobuf.Timestamp;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.proto.collector.ActionTypeProto;
import ru.practicum.ewm.stats.proto.collector.UserActionControllerGrpc;
import ru.practicum.ewm.stats.proto.collector.UserActionProto;

import java.time.Instant;

@Component
@Slf4j
public class CollectorGrpcClient {

    @GrpcClient("collector")
    private UserActionControllerGrpc.UserActionControllerBlockingStub collectorStub;

    public void sendView(long userId, long eventId) {
        send(userId, eventId, ActionTypeProto.ACTION_VIEW);
    }

    public void sendRegister(long userId, long eventId) {
        send(userId, eventId, ActionTypeProto.ACTION_REGISTER);
    }

    public void sendLike(long userId, long eventId) {
        send(userId, eventId, ActionTypeProto.ACTION_LIKE);
    }

    private void send(long userId, long eventId, ActionTypeProto actionType) {
        try {
            Instant now = Instant.now();
            UserActionProto proto = UserActionProto.newBuilder()
                    .setUserId(userId)
                    .setEventId(eventId)
                    .setActionType(actionType)
                    .setTimestamp(Timestamp.newBuilder()
                            .setSeconds(now.getEpochSecond())
                            .setNanos(now.getNano())
                            .build())
                    .build();
            collectorStub.collectUserAction(proto);
            log.debug("Sent action {} for userId={}, eventId={}", actionType, userId, eventId);
        } catch (Exception e) {
            log.warn("Failed to send action to Collector: userId={}, eventId={}, type={}: {}",
                    userId, eventId, actionType, e.getMessage());
        }
    }
}