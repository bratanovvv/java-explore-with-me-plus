package ru.practicum.compilation.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
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
