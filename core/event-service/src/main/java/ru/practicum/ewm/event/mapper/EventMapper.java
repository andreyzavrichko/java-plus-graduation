package ru.practicum.ewm.event.mapper;

import org.mapstruct.*;
import ru.practicum.ewm.api.dto.UserInternalDto;
import ru.practicum.ewm.category.mapper.CategoryMapper;
import ru.practicum.ewm.event.dto.EventDto;
import ru.practicum.ewm.event.dto.EventDtoExtended;
import ru.practicum.ewm.event.dto.EventDtoShort;
import ru.practicum.ewm.event.dto.EventDtoShortWithoutViews;
import ru.practicum.ewm.event.dto.request.CreateEventDto;
import ru.practicum.ewm.event.dto.request.UpdateEventDto;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.user.dto.UserShortDto;

import static ru.practicum.ewm.event.model.State.DEFAULT_STATE;

@Mapper(componentModel = "spring", uses = {LocationMapper.class, CategoryMapper.class})
public interface EventMapper {

    @Mapping(target = "id",                ignore = true)
    @Mapping(target = "createdOn",         ignore = true)
    @Mapping(target = "initiatorId",       ignore = true)
    @Mapping(target = "category",          ignore = true)
    @Mapping(target = "publishedOn",       ignore = true)
    @Mapping(target = "state",             constant = DEFAULT_STATE)
    @Mapping(target = "requestModeration", source = "requestModeration", defaultValue = "true")
    @Mapping(target = "paid",              source = "paid",              defaultValue = "false")
    @Mapping(target = "participantLimit",  source = "participantLimit",  defaultValue = "0")
    Event toEntity(CreateEventDto dto);

    @Mapping(target = "id",        source = "event.id")
    @Mapping(target = "initiator", source = "initiator")
    @Mapping(target = "rating",    ignore = true)
    EventDto toDto(Event event, UserShortDto initiator);

    @Mapping(target = "id",               source = "event.id")
    @Mapping(target = "rating",           source = "rating")
    @Mapping(target = "confirmedRequests", source = "confirmedRequests")
    @Mapping(target = "initiator",        source = "initiator")
    EventDtoExtended toExtendedDto(Event event, UserShortDto initiator,
                                   Double rating, Long confirmedRequests);

    @Mapping(target = "id",               source = "event.id")
    @Mapping(target = "rating",           source = "rating")
    @Mapping(target = "confirmedRequests", source = "confirmedRequests")
    @Mapping(target = "initiator",        source = "initiator")
    EventDtoShort toDtoShort(Event event, UserShortDto initiator,
                             Double rating, Long confirmedRequests);

    @Mapping(target = "id",               source = "event.id")
    @Mapping(target = "confirmedRequests", source = "confirmedRequests")
    @Mapping(target = "initiator",        source = "initiator")
    EventDtoShortWithoutViews toDtoShortWithoutViews(Event event, UserShortDto initiator,
                                                     Long confirmedRequests);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id",          ignore = true)
    @Mapping(target = "createdOn",   ignore = true)
    @Mapping(target = "category",    ignore = true)
    @Mapping(target = "publishedOn", ignore = true)
    @Mapping(target = "initiatorId", ignore = true)
    @Mapping(target = "state",       ignore = true)
    void updateEntity(UpdateEventDto dto, @MappingTarget Event event);

    default UserShortDto toUserShort(UserInternalDto dto) {
        if (dto == null) return null;
        return new UserShortDto(dto.id(), dto.name());
    }
}