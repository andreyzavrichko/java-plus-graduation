package ru.practicum.ewm.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.CreateHitDto;
import ru.practicum.ewm.HitDto;
import ru.practicum.ewm.ViewStatsDto;
import ru.practicum.ewm.service.StatsService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class StatsController {
    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private final StatsService statsService;

    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public HitDto createHit(@RequestBody @Valid CreateHitDto createDto) {
        log.info("Create hit request: {}", createDto);
        HitDto result = statsService.save(createDto);
        log.info("Create hit response: {}", result);
        return result;
    }

    @GetMapping("/stats")
    public List<ViewStatsDto> getStats(@RequestParam
                                       @DateTimeFormat(pattern = DATE_TIME_PATTERN) LocalDateTime start,
                                       @RequestParam
                                       @DateTimeFormat(pattern = DATE_TIME_PATTERN) LocalDateTime end,
                                       @RequestParam(required = false) List<String> uris,
                                       @RequestParam(defaultValue = "false") Boolean unique) {
        log.info("Get stats request. Start: {}, End: {}, Uris: {}, Unique: {}", start, end, uris, unique);
        List<ViewStatsDto> result = statsService.getStats(start, end, uris, unique);
        log.info("Get stats response: {}", result);
        return result;
    }
}