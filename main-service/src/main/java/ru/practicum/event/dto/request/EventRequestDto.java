package ru.practicum.event.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;
import ru.practicum.event.dto.common.LocationDto;
import ru.practicum.utils.formats.DateFormats;

import java.time.LocalDateTime;

/**
 * Тело создания и частичного обновления события.
 * <p>
 * Для создания обязательность полей задаётся группой {@link Create};
 * при обновлении все поля опциональны.
 */
@Data
public class EventRequestDto {

    /**
     * Группа валидации.
     */
    public interface Create {
    }

    @NotBlank(groups = Create.class)
    @Size(min = 20, max = 2000)
    private String annotation;

    @NotNull(groups = Create.class)
    private Long category;

    @NotBlank(groups = Create.class)
    @Size(min = 20, max = 7000)
    private String description;

    @NotNull(groups = Create.class)
    @JsonFormat(pattern = DateFormats.DATE_TIME)
    private LocalDateTime eventDate;

    @NotNull(groups = Create.class)
    @Valid
    private LocationDto location;

    private Boolean paid;

    @PositiveOrZero
    private Integer participantLimit;

    private Boolean requestModeration;

    @NotBlank(groups = Create.class)
    @Size(min = 3, max = 120)
    private String title;

    private EventStateAction stateAction;
}
