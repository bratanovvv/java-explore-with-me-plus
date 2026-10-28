package ru.practicum.event.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.category.mapper.CategoryMapper;
import ru.practicum.category.model.Category;
import ru.practicum.event.dto.response.EventDto;
import ru.practicum.event.model.Event;
import ru.practicum.stats.dto.ViewStatsDto;
import ru.practicum.user.mapper.UserMapper;
import ru.practicum.user.model.User;
import ru.practicum.utils.integrations.StatsService;
import ru.practicum.utils.params.PageParams;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventEnricherTest {

    @Mock
    private StatsService statsService;

    private EventEnricher eventEnricher;

    @BeforeEach
    void setUp() {
        eventEnricher = new EventEnricher(
                new EventMapper(new CategoryMapper(), new UserMapper(), new LocationMapper()), statsService);
    }

    @Test
    void toDto_shouldSetViewsFromStats() {
        Event event = buildEvent(1L);
        when(statsService.getViews(any(LocalDateTime.class), any(LocalDateTime.class), anyList(), eq(true)))
                .thenReturn(List.of(new ViewStatsDto("ewm-main-service", "/events/1", 5L)));

        EventDto dto = eventEnricher.toDto(event);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getViews()).isEqualTo(5L);
        assertThat(dto.getConfirmedRequests()).isZero();
        assertThat(dto.getCategory().getId()).isEqualTo(10L);
        assertThat(dto.getInitiator().getId()).isEqualTo(20L);
    }

    @Test
    void toDtos_shouldLoadViewsInSingleBatch() {
        when(statsService.getViews(any(LocalDateTime.class), any(LocalDateTime.class), anyList(), eq(true)))
                .thenReturn(List.of(
                        new ViewStatsDto("ewm-main-service", "/events/1", 5L),
                        new ViewStatsDto("ewm-main-service", "/events/2", 2L)));

        List<EventDto> dtos = eventEnricher.toDtos(List.of(buildEvent(1L), buildEvent(2L)));

        assertThat(dtos).hasSize(2);
        assertThat(dtos).extracting(EventDto::getViews).containsExactly(5L, 2L);
        verify(statsService).getViews(any(LocalDateTime.class), any(LocalDateTime.class),
                eq(List.of("/events/1", "/events/2")), eq(true));
    }

    @Test
    void toDtos_shouldReturnZeroViewsWhenStatsAreMissing() {
        when(statsService.getViews(any(LocalDateTime.class), any(LocalDateTime.class), anyList(), anyBoolean()))
                .thenReturn(List.of());

        List<EventDto> dtos = eventEnricher.toDtos(List.of(buildEvent(3L)));

        assertThat(dtos).hasSize(1);
        assertThat(dtos.getFirst().getViews()).isZero();
    }

    @Test
    void toDtos_shouldNotCallStatsForEmptyList() {
        assertThat(eventEnricher.toDtos(List.of())).isEmpty();
        verifyNoInteractions(statsService);
    }

    @Test
    void toDtosByViews_shouldSortDescendingAndSlice() {
        when(statsService.getViews(any(LocalDateTime.class), any(LocalDateTime.class), anyList(), eq(true)))
                .thenReturn(List.of(
                        new ViewStatsDto("ewm-main-service", "/events/1", 1L),
                        new ViewStatsDto("ewm-main-service", "/events/2", 5L),
                        new ViewStatsDto("ewm-main-service", "/events/3", 3L)));

        List<EventDto> dtos = eventEnricher.toDtosByViews(
                List.of(buildEvent(1L), buildEvent(2L), buildEvent(3L)), new PageParams(1, 1));

        assertThat(dtos).extracting(EventDto::getId).containsExactly(3L);
    }

    @Test
    void toDtosByViews_shouldReturnEmptyWhenOffsetIsTooBig() {
        List<EventDto> dtos = eventEnricher.toDtosByViews(List.of(), new PageParams(10, 10));

        assertThat(dtos).isEmpty();
    }

    private Event buildEvent(Long id) {
        Category category = Category.builder().id(10L).name("Концерты").build();
        User initiator = User.builder().id(20L).name("Иван").email("ivan@mail.ru").build();
        return Event.builder()
                .id(id)
                .title("Событие " + id)
                .annotation("Аннотация события " + id)
                .category(category)
                .initiator(initiator)
                .paid(false)
                .participantLimit(0)
                .requestModeration(true)
                .eventDate(LocalDateTime.now().plusDays(1))
                .createdOn(LocalDateTime.now())
                .build();
    }
}
