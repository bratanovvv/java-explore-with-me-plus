package ru.practicum.compilation.controller;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.compilation.service.CompilationService;
import ru.practicum.event.dto.response.EventDto;
import ru.practicum.utils.interfaces.Mapper;
import ru.practicum.utils.params.PageParams;

import java.util.List;

@RestController
@RequestMapping("/compilations")
@Validated
public class PublicCompilationController {

    private final CompilationService compilationService;
    private final Mapper<Compilation, CompilationDto> comMapper;

    public PublicCompilationController(CompilationService compilationService,
                                       Mapper<Compilation, CompilationDto> comMapper) {
        this.compilationService = compilationService;
        this.comMapper = comMapper;
    }

    @GetMapping
    @JsonView(EventDto.FullEvent.class)
    public List<CompilationDto> getCompilation(
            @RequestParam(name = "pinned", required = false) Boolean pinned,
            @RequestParam(defaultValue = PageParams.DEFAULT_FROM) @PositiveOrZero int from,
            @RequestParam(defaultValue = PageParams.DEFAULT_SIZE) @Positive int size
    ) {
        return comMapper.toDtoList(compilationService.getList(pinned, new PageParams(from, size)));
    }

    @GetMapping("/{compId}")
    @JsonView(EventDto.FullEvent.class)
    public CompilationDto getCompilationById(@PathVariable @PositiveOrZero Long compId) {
        return comMapper.toDto(compilationService.getCompilationById(compId));
    }
}
