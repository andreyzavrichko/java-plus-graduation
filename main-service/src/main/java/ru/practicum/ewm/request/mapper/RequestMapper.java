package ru.practicum.ewm.request.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.practicum.ewm.request.dto.ParticipationRequestDto;
import ru.practicum.ewm.request.model.ParticipationRequest;
import ru.practicum.ewm.request.model.RequestStatus;

import static ru.practicum.ewm.sharing.constants.AppConstants.DATE_TIME_FORMAT;

@Mapper(componentModel = "spring")
public interface RequestMapper {

    @Mapping(target = "created", dateFormat = DATE_TIME_FORMAT)
    @Mapping(target = "event", source = "event.id")
    @Mapping(target = "requester", source = "requester.id")
    @Mapping(target = "status", source = "status", qualifiedByName = "statusToString")
    ParticipationRequestDto toDto(ParticipationRequest request);

    @Named("statusToString")
    default String statusToString(RequestStatus status) {
        return status != null ? status.toString() : null;
    }
}
