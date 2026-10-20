package ru.practicum.stats.mapper.impl;

import org.springframework.stereotype.Component;
import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.mapper.Mapper;
import ru.practicum.stats.model.EndpointHit;

@Component
public class EndpointHitMapper implements Mapper<EndpointHit, EndpointHitDto> {

    @Override
    public EndpointHitDto toDto(EndpointHit endpointHit) {
        return new EndpointHitDto(
                endpointHit.getId(),
                endpointHit.getApp(),
                endpointHit.getUri(),
                endpointHit.getIp(),
                endpointHit.getTimestamp());
    }

    @Override
    public EndpointHit toEntity(EndpointHitDto endpointHitDto) {
        return new EndpointHit(
                null,
                endpointHitDto.getApp(),
                endpointHitDto.getUri(),
                endpointHitDto.getIp(),
                endpointHitDto.getTimestamp());
    }
}
