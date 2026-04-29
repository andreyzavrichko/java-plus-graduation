package ru.practicum.ewm.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.practicum.ewm.api.dto.UserInternalDto;

import java.util.List;
import java.util.Map;

@FeignClient(name = "user-service", fallback = UserFeignClientFallback.class)
public interface UserFeignClient {

    @GetMapping("/internal/users/{userId}")
    UserInternalDto getUser(@PathVariable("userId") Long userId);

    @PostMapping("/internal/users/batch")
    Map<Long, UserInternalDto> getUsersBatch(@RequestBody List<Long> ids);

    @GetMapping("/internal/users/exists/{userId}")
    boolean exists(@PathVariable("userId") Long userId);
}
