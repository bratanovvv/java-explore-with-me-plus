package ru.practicum.stats.mapper.impl;

import org.junit.jupiter.api.Test;
import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.dto.ViewStatsDto;
import ru.practicum.stats.model.EndpointHit;
import ru.practicum.stats.model.projection.ViewStats;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MappersTest {

    private static final LocalDateTime TIMESTAMP = LocalDateTime.of(2026, 10, 20, 10, 0, 0);

    private final EndpointHitMapper endpointHitMapper = new EndpointHitMapper();
    private final ViewStatsMapper viewStatsMapper = new ViewStatsMapper();

    @Test
    void endpointHitMapper_toEntity_shouldIgnoreId() {
        EndpointHitDto dto = new EndpointHitDto(99L, "app", "/uri", "1.1.1.1", TIMESTAMP);

        EndpointHit entity = endpointHitMapper.toEntity(dto);

        assertThat(entity.getId()).isNull();
        assertThat(entity.getApp()).isEqualTo("app");
        assertThat(entity.getUri()).isEqualTo("/uri");
        assertThat(entity.getIp()).isEqualTo("1.1.1.1");
        assertThat(entity.getTimestamp()).isEqualTo(TIMESTAMP);
    }

    @Test
    void endpointHitMapper_toDto_shouldMapAllFields() {
        EndpointHit entity = new EndpointHit(1L, "app", "/uri", "1.1.1.1", TIMESTAMP);

        EndpointHitDto dto = endpointHitMapper.toDto(entity);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getApp()).isEqualTo("app");
        assertThat(dto.getUri()).isEqualTo("/uri");
        assertThat(dto.getIp()).isEqualTo("1.1.1.1");
        assertThat(dto.getTimestamp()).isEqualTo(TIMESTAMP);
    }

    @Test
    void viewStatsMapper_toDto_shouldMapAllFields() {
        ViewStats viewStats = new ViewStats("app", "/uri", 5L);

        ViewStatsDto dto = viewStatsMapper.toDto(viewStats);

        assertThat(dto.getApp()).isEqualTo("app");
        assertThat(dto.getUri()).isEqualTo("/uri");
        assertThat(dto.getHits()).isEqualTo(5L);
    }

    @Test
    void viewStatsMapper_toEntity_shouldMapAllFields() {
        ViewStatsDto dto = new ViewStatsDto("app", "/uri", 5L);

        ViewStats viewStats = viewStatsMapper.toEntity(dto);

        assertThat(viewStats.getApp()).isEqualTo("app");
        assertThat(viewStats.getUri()).isEqualTo("/uri");
        assertThat(viewStats.getHits()).isEqualTo(5L);
    }

    @Test
    void viewStatsMapper_toDtoList_shouldMapAllElements() {
        List<ViewStats> stats = List.of(new ViewStats("app", "/uri", 5L), new ViewStats("app2", "/uri2", 7L));

        List<ViewStatsDto> dtos = viewStatsMapper.toDtoList(stats);

        assertThat(dtos).hasSize(2);
        assertThat(dtos.get(0).getHits()).isEqualTo(5L);
        assertThat(dtos.get(1).getHits()).isEqualTo(7L);
    }
}
