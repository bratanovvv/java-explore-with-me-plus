package ru.practicum.compilation.mapper;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.compilation.dto.RequestCompilationDto;
import ru.practicum.compilation.dto.ResponseCompilationDto;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.model.Event;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class CompilationMapper {
    private EventMapper eMapper;

    public ResponseCompilationDto toDto(Compilation entity) {
        return ResponseCompilationDto.builder()
                .id(entity.getId())
                .events(entity.getEvents() == null ? new HashSet<>() :
                        entity.getEvents().stream().map(eMapper::toDto).collect(Collectors.toSet()))
                .pinned(entity.getPinned())
                .title(entity.getTitle())
                .build();
    }

    public Compilation toEntity(Set<Event> events, RequestCompilationDto dto) {
        return Compilation.builder()
                .pinned(dto.getPinned())
                .title(dto.getTitle())
                .events(events)
                .build();
    }

    public List<ResponseCompilationDto> toDtoList(List<Compilation> entities) {
        return entities.stream().map(this::toDto).toList();
    }
}
