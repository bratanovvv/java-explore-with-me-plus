package ru.practicum.compilation.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.compilation.dto.RequestCompilationDto;
import ru.practicum.compilation.dto.ResponseCompilationDto;
import ru.practicum.compilation.service.CompilationService;

@RestController
@RequestMapping("/admin/compilations")
@AllArgsConstructor
@Validated
public class AdminCompilationController {

    public final CompilationService compilationService;

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseCompilationDto create(@RequestBody @Valid RequestCompilationDto dto) {
        return compilationService.create(dto);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{compId}")
    public void delete(@PathVariable @PositiveOrZero Long compId) {
        compilationService.delete(compId);
    }

    @PatchMapping("/{compId}")
    public ResponseCompilationDto update(@PathVariable @PositiveOrZero Long compId,
                                         @RequestBody RequestCompilationDto dto) {
        return compilationService.update(compId, dto);
    }
}
