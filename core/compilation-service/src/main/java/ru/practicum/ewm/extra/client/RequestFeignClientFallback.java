package ru.practicum.ewm.extra.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class RequestFeignClientFallback implements RequestFeignClient {
    @Override
    public Map<Long, Long> confirmedCounts(List<Long> eventIds) {
        log.warn("request-service unavailable: confirmedCounts fallback — returning zeros");
        return Map.of();
    }
}
