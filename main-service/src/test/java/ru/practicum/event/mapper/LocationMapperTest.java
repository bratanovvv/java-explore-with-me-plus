package ru.practicum.event.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.event.dto.common.LocationDto;
import ru.practicum.event.model.Location;

import static org.assertj.core.api.Assertions.assertThat;

class LocationMapperTest {

    private LocationMapper locationMapper;

    @BeforeEach
    void setUp() {
        locationMapper = new LocationMapper();
    }

    @Test
    void toDto_shouldMapCoordinates() {
        LocationDto dto = locationMapper.toDto(new Location(55.75, 37.62));

        assertThat(dto.getLat()).isEqualTo(55.75);
        assertThat(dto.getLon()).isEqualTo(37.62);
    }

    @Test
    void toEntity_shouldMapCoordinates() {
        Location location = locationMapper.toEntity(new LocationDto(55.75, 37.62));

        assertThat(location.getLat()).isEqualTo(55.75);
        assertThat(location.getLon()).isEqualTo(37.62);
    }

    @Test
    void toDto_shouldReturnNullForNullLocation() {
        assertThat(locationMapper.toDto(null)).isNull();
    }

    @Test
    void toEntity_shouldReturnNullForNullDto() {
        assertThat(locationMapper.toEntity(null)).isNull();
    }
}
