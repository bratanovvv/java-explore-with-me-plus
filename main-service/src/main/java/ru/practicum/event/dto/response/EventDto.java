package ru.practicum.event.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.event.dto.common.LocationDto;
import ru.practicum.event.model.EventState;
import ru.practicum.user.dto.UserDto;
import ru.practicum.utils.formats.DateFormats;

import java.time.LocalDateTime;

/**
 * Информация о событии.
 * <p>
 * Состав ответа задаётся {@link JsonView}.
 * Поля краткого представления помечены {@link ShortEvent}, поля полного — {@link FullEvent}.
 * <p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventDto {

    public interface ShortEvent extends UserDto.ShortUser {
    }

    public interface FullEvent extends ShortEvent {
    }

    @JsonView(ShortEvent.class)
    private Long id;

    @JsonView(ShortEvent.class)
    private String title;

    @JsonView(ShortEvent.class)
    private String annotation;

    @JsonView(ShortEvent.class)
    private CategoryDto category;

    @JsonView(ShortEvent.class)
    private Boolean paid;

    @JsonFormat(pattern = DateFormats.DATE_TIME)
    @JsonView(ShortEvent.class)
    private LocalDateTime eventDate;

    @JsonView(ShortEvent.class)
    private UserDto initiator;

    @Builder.Default
    @JsonView(ShortEvent.class)
    private Long views = 0L;

    @Builder.Default
    @JsonView(ShortEvent.class)
    private Long confirmedRequests = 0L;

    @JsonView(FullEvent.class)
    private String description;

    @JsonView(FullEvent.class)
    private Integer participantLimit;

    @JsonView(FullEvent.class)
    private EventState state;

    @JsonFormat(pattern = DateFormats.DATE_TIME)
    @JsonView(FullEvent.class)
    private LocalDateTime createdOn;

    @JsonFormat(pattern = DateFormats.DATE_TIME)
    @JsonView(FullEvent.class)
    private LocalDateTime publishedOn;

    @JsonView(FullEvent.class)
    private LocationDto location;

    @JsonView(FullEvent.class)
    private Boolean requestModeration;
}
