package ru.practicum.ewm.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.api.dto.UserInternalDto;
import ru.practicum.ewm.exception.NotFoundException;

import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class UserFeignClientFallback implements UserFeignClient {

    @Override
    public UserInternalDto getUser(Long userId) {
        log.error("user-service unavailable: getUser({})", userId);
        throw new NotFoundException("User service unavailable. UserId=%d".formatted(userId));
    }

    @Override
    public Map<Long, UserInternalDto> getUsersBatch(List<Long> ids) {
        log.warn("user-service unavailable: getUsersBatch — returning empty map");
        return Map.of();
    }

    @Override
    public boolean exists(Long userId) {
        log.warn("user-service unavailable: exists({}) — returning false", userId);
        return false;
    }
}
