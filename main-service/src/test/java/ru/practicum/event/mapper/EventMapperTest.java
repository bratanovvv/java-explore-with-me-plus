package ru.practicum.event.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.category.mapper.CategoryMapper;
import ru.practicum.category.model.Category;
import ru.practicum.event.dto.common.LocationDto;
import ru.practicum.event.dto.request.EventRequestDto;
import ru.practicum.event.dto.request.EventStateAction;
import ru.practicum.event.dto.response.EventDto;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventState;
import ru.practicum.event.model.Location;
import ru.practicum.user.mapper.UserMapper;
import ru.practicum.user.model.User;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class EventMapperTest {

    private EventMapper eventMapper;

    @BeforeEach
    void setUp() {
        eventMapper = new EventMapper(new CategoryMapper(), new UserMapper(), new LocationMapper());
    }

    @Test
    void toDto_shouldMapAllFields() {
        Event event = buildEvent();

        EventDto dto = eventMapper.toDto(event);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getTitle()).isEqualTo("Событие");
        assertThat(dto.getAnnotation()).isEqualTo("Аннотация события");
        assertThat(dto.getCategory().getId()).isEqualTo(2L);
        assertThat(dto.getCategory().getName()).isEqualTo("Концерты");
        assertThat(dto.getInitiator().getId()).isEqualTo(3L);
        assertThat(dto.getInitiator().getName()).isEqualTo("Иван");
        assertThat(dto.getPaid()).isFalse();
        assertThat(dto.getParticipantLimit()).isEqualTo(10);
        assertThat(dto.getRequestModeration()).isTrue();
        assertThat(dto.getState()).isEqualTo(EventState.PUBLISHED);
        assertThat(dto.getLocation().getLat()).isEqualTo(55.75);
        assertThat(dto.getLocation().getLon()).isEqualTo(37.62);
        assertThat(dto.getPublishedOn()).isNotNull();
        assertThat(dto.getViews()).isZero();
        assertThat(dto.getConfirmedRequests()).isZero();
    }

    @Test
    void toEntity_shouldMapProvidedFields() {
        EventRequestDto request = new EventRequestDto();
        request.setAnnotation("Аннотация события");
        request.setCategory(2L);
        request.setDescription("Описание события");
        request.setEventDate(LocalDateTime.now().plusDays(1));
        request.setLocation(new LocationDto(55.75, 37.62));
        request.setPaid(true);
        request.setParticipantLimit(10);
        request.setRequestModeration(false);
        request.setTitle("Событие");
        request.setStateAction(EventStateAction.CANCEL_REVIEW);

        Event event = eventMapper.toEntity(request);

        assertThat(event.getAnnotation()).isEqualTo("Аннотация события");
        assertThat(event.getCategory().getId()).isEqualTo(2L);
        assertThat(event.getDescription()).isEqualTo("Описание события");
        assertThat(event.getEventDate()).isEqualTo(request.getEventDate());
        assertThat(event.getLocation().getLat()).isEqualTo(55.75);
        assertThat(event.getPaid()).isTrue();
        assertThat(event.getParticipantLimit()).isEqualTo(10);
        assertThat(event.getRequestModeration()).isFalse();
        assertThat(event.getTitle()).isEqualTo("Событие");
        assertThat(event.getInitiator()).isNull();
        assertThat(event.getState()).isNull();
        assertThat(event.getCreatedOn()).isNull();
    }

    @Test
    void toEntity_shouldKeepNullsForPartialUpdate() {
        Event event = eventMapper.toEntity(new EventRequestDto());

        assertThat(event.getAnnotation()).isNull();
        assertThat(event.getCategory()).isNull();
        assertThat(event.getDescription()).isNull();
        assertThat(event.getEventDate()).isNull();
        assertThat(event.getLocation()).isNull();
        assertThat(event.getPaid()).isNull();
        assertThat(event.getParticipantLimit()).isNull();
        assertThat(event.getRequestModeration()).isNull();
        assertThat(event.getTitle()).isNull();
    }

    private Event buildEvent() {
        Category category = Category.builder().id(2L).name("Концерты").build();
        User initiator = User.builder().id(3L).name("Иван").email("ivan@mail.ru").build();
        return Event.builder()
                .id(1L)
                .title("Событие")
                .annotation("Аннотация события")
                .description("Описание события")
                .category(category)
                .initiator(initiator)
                .paid(false)
                .participantLimit(10)
                .requestModeration(true)
                .state(EventState.PUBLISHED)
                .eventDate(LocalDateTime.now().plusDays(1))
                .createdOn(LocalDateTime.now())
                .publishedOn(LocalDateTime.now())
                .location(new Location(55.75, 37.62))
                .build();
    }
}
