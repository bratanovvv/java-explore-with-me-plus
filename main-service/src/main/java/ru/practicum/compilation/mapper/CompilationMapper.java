package ru.practicum.compilation.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.event.mapper.EventMapper;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CompilationMapper {
    private final EventMapper eMapper;

    public CompilationMapper(EventMapper eMapper) {
        this.eMapper = eMapper;
    }

    public CompilationDto toDto(Compilation entity) {
        return CompilationDto.builder()
                .id(entity.getId())
                .events(entity.getEvents() == null ? new HashSet<>() :
                        entity.getEvents().stream().map(eMapper::toDto).collect(Collectors.toSet()))
                .pinned(entity.getPinned())
                .title(entity.getTitle())
                .build();
    }

    public Compilation toEntity(CompilationDto dto) {
        return Compilation.builder()
                .pinned(dto.getPinned())
                .title(dto.getTitle())
                .build();
    }

    public List<CompilationDto> toDtoList(List<Compilation> entities) {
        return entities.stream().map(this::toDto).toList();
    }
}
