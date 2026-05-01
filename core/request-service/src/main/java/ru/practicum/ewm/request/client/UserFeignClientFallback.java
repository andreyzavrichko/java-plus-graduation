package ru.practicum.ewm.request.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UserFeignClientFallback implements UserFeignClient {

    @Override
    public boolean exists(Long userId) {
        log.warn("user-service unavailable: exists({}) — returning true (fallback)", userId);
        return true;
    }
}
