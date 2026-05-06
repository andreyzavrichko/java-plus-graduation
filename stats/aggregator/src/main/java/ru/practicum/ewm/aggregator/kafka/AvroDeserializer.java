package ru.practicum.ewm.aggregator.kafka;

import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.common.serialization.Deserializer;

import java.io.IOException;


public class AvroDeserializer<T extends SpecificRecordBase> implements Deserializer<T> {

    private final Class<T> targetType;

    public AvroDeserializer(Class<T> targetType) {
        this.targetType = targetType;
    }

    @Override
    public T deserialize(String topic, byte[] data) {
        if (data == null) return null;
        try {
            SpecificDatumReader<T> reader = new SpecificDatumReader<>(targetType);
            BinaryDecoder decoder = DecoderFactory.get().binaryDecoder(data, null);
            return reader.read(null, decoder);
        } catch (IOException e) {
            throw new RuntimeException(
                    "Avro deserialization error for topic [%s]".formatted(topic), e);
        }
    }
}