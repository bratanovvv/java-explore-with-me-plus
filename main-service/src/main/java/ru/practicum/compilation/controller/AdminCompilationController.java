package ru.practicum.compilation.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.mapper.CompilationMapper;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.compilation.service.CompilationService;

@RestController
@RequestMapping("/admin/compilations")
@Validated
public class AdminCompilationController {

    public final CompilationService compilationService;
    public final CompilationMapper comMapper;

    public AdminCompilationController(CompilationService compilationService, CompilationMapper comMapper) {
        this.compilationService = compilationService;
        this.comMapper = comMapper;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompilationDto create(@RequestBody @Valid CompilationDto dto) {
        Compilation response = compilationService.create(comMapper.toEntity(dto), dto.getEventsId());
        return comMapper.toDto(response);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{compId}")
    public void delete(@PathVariable @PositiveOrZero Long compId) {
        compilationService.delete(compId);
    }

    @PatchMapping("/{compId}")
    public CompilationDto update(@PathVariable @PositiveOrZero Long compId,
                                 @RequestBody CompilationDto dto) {
        return comMapper.toDto(compilationService.update(compId, dto));
    }
}
