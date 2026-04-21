package ru.practicum.ewm.service;

import ru.practicum.ewm.CreateHitDto;
import ru.practicum.ewm.HitDto;
import ru.practicum.ewm.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsService {

    HitDto save(CreateHitDto hit);

    List<ViewStatsDto> getStats(LocalDateTime start,
                                LocalDateTime end,
                                List<String> uris,
                                boolean unique);
}