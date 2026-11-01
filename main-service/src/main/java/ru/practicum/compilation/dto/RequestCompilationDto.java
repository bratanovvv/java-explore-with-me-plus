package ru.practicum.compilation.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.validator.constraints.Length;

import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestCompilationDto {

    private Set<Long> events;

    @NonNull
    @Builder.Default
    private Boolean pinned = false;

    @NotBlank
    @Length(max = 50)
    private String title;
}
