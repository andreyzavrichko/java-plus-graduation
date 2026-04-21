package ru.practicum.ewm.mapper;

import ru.practicum.ewm.CreateHitDto;
import ru.practicum.ewm.HitDto;
import ru.practicum.ewm.model.App;
import ru.practicum.ewm.model.Hit;

public class HitMapper {
    public static HitDto toHitDto(Hit hit) {
        return new HitDto(
                hit.getApp().getValue(),
                hit.getUri(),
                hit.getIp(),
                hit.getTimestamp()
        );
    }

    public static Hit toHit(CreateHitDto createRequest) {
        return Hit.builder()
                .app(App.fromString(createRequest.app()))
                .uri(createRequest.uri())
                .ip(createRequest.ip())
                .timestamp(createRequest.timestamp())
                .build();
    }
}