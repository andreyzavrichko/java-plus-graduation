package ru.practicum.ewm.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.CreateHitDto;
import ru.practicum.ewm.HitDto;
import ru.practicum.ewm.ViewStatsDto;
import ru.practicum.ewm.exception.ValidationException;
import ru.practicum.ewm.mapper.HitMapper;
import ru.practicum.ewm.model.Hit;
import ru.practicum.ewm.repository.JdbcStatsRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatsServiceImpl implements StatsService {
    private final JdbcStatsRepository repository;

    @Override
    public HitDto save(CreateHitDto createHitDto) {
        Hit hit = HitMapper.toHit(createHitDto);
        hit = repository.save(hit);
        return HitMapper.toHitDto(hit);
    }

    @Override
    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        List<String> urisList = (uris != null) ? uris : Collections.emptyList();
        if (end.isBefore(start)) {
            log.warn("Invalid date range: end date [{}] is before start date [{}]", end, start);
            throw new ValidationException("End is before start");
        }
        return repository.getStats(start, end, urisList, unique);
    }
}