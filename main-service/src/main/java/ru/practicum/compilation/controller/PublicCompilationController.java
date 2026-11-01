package ru.practicum.compilation.controller;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.compilation.dto.ResponseCompilationDto;
import ru.practicum.compilation.service.CompilationService;
import ru.practicum.utils.params.PageParams;

import java.util.List;

@RestController
@RequestMapping("/compilations")
@Validated
@AllArgsConstructor
public class PublicCompilationController {

    private final CompilationService compilationService;

    @GetMapping()
    public List<ResponseCompilationDto> getCompilation(
            @RequestParam(name = "pinned", required = false, defaultValue = "false") Boolean pinned,
            @RequestParam(defaultValue = PageParams.DEFAULT_FROM) @PositiveOrZero int from,
            @RequestParam(defaultValue = PageParams.DEFAULT_SIZE) @Positive int size
    ) {
        return compilationService.getList(pinned, new PageParams(from, size));
    }

    @GetMapping("/{compId}")
    public ResponseCompilationDto getCompilationById(@PathVariable @PositiveOrZero Long compId) {
        return compilationService.getById(compId);
    }
}
