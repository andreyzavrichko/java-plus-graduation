package ru.practicum.ewm.sharing;

import lombok.extern.slf4j.Slf4j;
import ru.practicum.ewm.exception.NotFoundException;

@Slf4j
public abstract class BaseService {

    protected NotFoundException throwNotFound(Long entityId, EntityName entityName) {
        String className = entityName.getValue();
        log.warn("{} with ID {} not found", className, entityId);
        return new NotFoundException("%s with ID %s not found".formatted(entityName, entityId));
    }
}
