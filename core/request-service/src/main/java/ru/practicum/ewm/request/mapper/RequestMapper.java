package ru.practicum.ewm.request.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.ewm.request.dto.ParticipationRequestDto;
import ru.practicum.ewm.request.model.ParticipationRequest;

@Mapper(componentModel = "spring")
public interface RequestMapper {

    @Mapping(target = "event",     source = "eventId")
    @Mapping(target = "requester", source = "requesterId")
    @Mapping(target = "status",    expression = "java(r.getStatus().name())")
    ParticipationRequestDto toDto(ParticipationRequest r);

}
