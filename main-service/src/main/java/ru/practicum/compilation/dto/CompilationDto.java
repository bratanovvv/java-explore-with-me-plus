package ru.practicum.compilation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.validator.constraints.Length;
import ru.practicum.event.dto.response.EventDto;

import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompilationDto {

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    @JsonProperty(value = "events", access = JsonProperty.Access.WRITE_ONLY)
    private Set<Long> eventsId;

    @JsonView(EventDto.ShortEvent.class)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Set<EventDto> events;

    @Builder.Default
    private Boolean pinned = false;

    @NotBlank
    @Length(max = 50)
    private String title;
}
