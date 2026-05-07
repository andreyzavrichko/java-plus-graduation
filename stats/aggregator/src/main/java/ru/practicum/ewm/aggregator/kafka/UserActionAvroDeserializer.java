package ru.practicum.ewm.aggregator.kafka;

import ru.practicum.ewm.stats.avro.UserActionAvro;

public class UserActionAvroDeserializer extends AvroDeserializer<UserActionAvro> {
    public UserActionAvroDeserializer() {
        super(UserActionAvro.class);
    }
}