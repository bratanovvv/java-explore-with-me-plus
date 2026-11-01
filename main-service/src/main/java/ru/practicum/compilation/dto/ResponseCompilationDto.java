package ru.practicum.compilation.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import ru.practicum.event.dto.response.EventDto;

import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseCompilationDto {

    private Long id;

    private Set<EventDto> events;

    @NonNull
    private Boolean pinned;

    @NotBlank

    private String title;
}
