package ru.practicum.compilation.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.utils.interfaces.Mapper;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class CompilationMapper implements Mapper<Compilation, CompilationDto> {
    private final EventMapper eMapper;

    public CompilationMapper(EventMapper eMapper) {
        this.eMapper = eMapper;
    }

    @Override
    public CompilationDto toDto(Compilation entity) {
        return CompilationDto.builder()
                .id(entity.getId())
                .events(entity.getEvents() == null ? Set.of() :
                        entity.getEvents().stream().map(eMapper::toDto).collect(Collectors.toSet()))
                .pinned(entity.getPinned())
                .title(entity.getTitle())
                .build();
    }

    @Override
    public Compilation toEntity(CompilationDto dto) {
        return Compilation.builder()
                .pinned(dto.getPinned())
                .title(dto.getTitle())
                .build();
    }
}
