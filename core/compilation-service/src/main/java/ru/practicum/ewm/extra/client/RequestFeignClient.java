package ru.practicum.ewm.extra.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

@FeignClient(name = "request-service", fallback = RequestFeignClientFallback.class)
public interface RequestFeignClient {
    @PostMapping("/internal/requests/confirmed-counts")
    Map<Long, Long> confirmedCounts(@RequestBody List<Long> eventIds);
}
