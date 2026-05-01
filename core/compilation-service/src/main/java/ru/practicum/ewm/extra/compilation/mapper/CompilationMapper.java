package ru.practicum.ewm.extra.compilation.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.ewm.api.dto.EventInternalDto;
import ru.practicum.ewm.api.dto.UserInternalDto;
import ru.practicum.ewm.extra.compilation.dto.*;
import ru.practicum.ewm.extra.compilation.model.Compilation;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Mapper(componentModel = "spring")
public interface CompilationMapper {

    @Mapping(target = "events", ignore = true)
    CompilationDto toDto(Compilation compilation);

    @Mapping(target = "category.id",   source = "categoryId")
    @Mapping(target = "category.name", source = "categoryName")
    @Mapping(target = "confirmedRequests", constant = "0L")
    @Mapping(target = "initiator",     ignore = true)
    EventShortDto toEventShort(EventInternalDto event);

    @Mapping(target = "id",   source = "id")
    @Mapping(target = "name", source = "name")
    UserShortDto toUserShort(UserInternalDto user);

    default CompilationDto toDto(Compilation compilation,
                                 Map<Long, EventInternalDto> eventsMap,
                                 Map<Long, Long> countsMap,
                                 Map<Long, UserInternalDto> usersMap) {
        List<EventShortDto> events = compilation.getEventIds().stream()
                .map(eventsMap::get)
                .filter(Objects::nonNull)
                .map(e -> {
                    EventShortDto dto = toEventShort(e);
                    UserInternalDto user = usersMap.get(e.initiatorId());
                    return new EventShortDto(
                            dto.id(),
                            dto.annotation(),
                            dto.category(),
                            countsMap.getOrDefault(e.id(), 0L),
                            dto.eventDate(),
                            user != null ? toUserShort(user) : null,
                            dto.paid(),
                            dto.title()
                    );
                })
                .toList();
        return new CompilationDto(compilation.getId(), compilation.getPinned(),
                compilation.getTitle(), events);
    }
}