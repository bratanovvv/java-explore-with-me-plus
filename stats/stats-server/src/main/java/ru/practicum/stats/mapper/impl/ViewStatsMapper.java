package ru.practicum.stats.mapper.impl;

import org.springframework.stereotype.Component;
import ru.practicum.stats.dto.ViewStatsDto;
import ru.practicum.stats.mapper.Mapper;
import ru.practicum.stats.model.projection.ViewStats;

@Component
public class ViewStatsMapper implements Mapper<ViewStats, ViewStatsDto> {

    @Override
    public ViewStatsDto toDto(ViewStats viewStats) {
        return new ViewStatsDto(viewStats.getApp(), viewStats.getUri(), viewStats.getHits());
    }

    @Override
    public ViewStats toEntity(ViewStatsDto viewStatsDto) {
        return new ViewStats(viewStatsDto.getApp(), viewStatsDto.getUri(), viewStatsDto.getHits());
    }
}
