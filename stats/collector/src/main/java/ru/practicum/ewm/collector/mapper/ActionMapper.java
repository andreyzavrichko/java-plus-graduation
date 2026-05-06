package ru.practicum.ewm.collector.mapper;

import com.google.protobuf.Timestamp;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.ewm.stats.proto.collector.ActionTypeProto;
import ru.practicum.ewm.stats.proto.collector.UserActionProto;

public final class ActionMapper {

    private ActionMapper() {
    }

    public static UserActionAvro toAvro(UserActionProto proto) {
        Timestamp ts = proto.getTimestamp();
        long millis = ts.getSeconds() * 1000L + ts.getNanos() / 1_000_000L;

        return UserActionAvro.newBuilder()
                .setUserId(proto.getUserId())
                .setEventId(proto.getEventId())
                .setActionType(toAvroType(proto.getActionType()))
                .setTimestamp(millis)
                .build();
    }

    private static ActionTypeAvro toAvroType(ActionTypeProto proto) {
        return switch (proto) {
            case ACTION_VIEW -> ActionTypeAvro.VIEW;
            case ACTION_REGISTER -> ActionTypeAvro.REGISTER;
            case ACTION_LIKE -> ActionTypeAvro.LIKE;
            default -> throw new IllegalArgumentException("Unknown action type: " + proto);
        };
    }
}