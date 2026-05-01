package ru.practicum.ewm.internal;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.ewm.api.dto.EventInternalDto;
import ru.practicum.ewm.event.model.Event;

@Mapper(componentModel = "spring")
public interface EventInternalMapper {

    @Mapping(target = "categoryId",   source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "state",        expression = "java(ru.practicum.ewm.api.dto.enums.EventStateInternal.valueOf(event.getState().name()))")
    EventInternalDto toInternalDto(Event event);
}