package ru.practicum.event.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.event.dto.common.LocationDto;
import ru.practicum.event.model.Location;
import ru.practicum.utils.interfaces.Mapper;

@Component
public class LocationMapper implements Mapper<Location, LocationDto> {

    @Override
    public LocationDto toDto(Location location) {
        if (location == null) {
            return null;
        }
        return new LocationDto(location.getLat(), location.getLon());
    }

    @Override
    public Location toEntity(LocationDto dto) {
        if (dto == null) {
            return null;
        }
        return new Location(dto.getLat(), dto.getLon());
    }
}
