package ru.practicum.ewm.event.dto.projection;

import ru.practicum.ewm.category.dto.CategoryInfo;
import ru.practicum.ewm.user.dto.UserInfoProjection;

import java.time.LocalDateTime;

public interface EventInfo {
    Long getId();

    String getTitle();

    String getAnnotation();

    LocalDateTime getEventDate();

    Boolean getPaid();

    UserInfoProjection getInitiator();

    CategoryInfo getCategory();
}
