package ru.practicum.ewm.event.mapper;

import org.mapstruct.Mapper;
import ru.practicum.ewm.event.dto.request.LocationBody;
import ru.practicum.ewm.event.model.Location;

@Mapper
public interface LocationMapper {

    Location toEntity(LocationBody dto);
}
