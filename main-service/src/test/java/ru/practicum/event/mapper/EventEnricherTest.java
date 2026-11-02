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
import ru.practicum.request.model.Request;
import ru.practicum.request.model.RequestStatus;
import ru.practicum.request.service.RequestService;
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

    @Mock
    private RequestService requestService;

    private EventEnricher eventEnricher;

    @BeforeEach
    void setUp() {
        eventEnricher = new EventEnricher(
                new EventMapper(new CategoryMapper(), new UserMapper(), new LocationMapper()),
                statsService,
                requestService);
    }

    @Test
    void toDto_shouldSetViewsFromStats() {
        Event event = buildEvent(1L);
        when(statsService.getViews(any(LocalDateTime.class), any(LocalDateTime.class), anyList(), eq(true)))
                .thenReturn(List.of(new ViewStatsDto("ewm-main-service", "/events/1", 5L)));
        stubNoConfirmedRequests();

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
        stubNoConfirmedRequests();

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
        stubNoConfirmedRequests();

        List<EventDto> dtos = eventEnricher.toDtos(List.of(buildEvent(3L)));

        assertThat(dtos).hasSize(1);
        assertThat(dtos.getFirst().getViews()).isZero();
    }

    @Test
    void toDtos_shouldNotCallStatsForEmptyList() {
        assertThat(eventEnricher.toDtos(List.of())).isEmpty();
        verifyNoInteractions(statsService, requestService);
    }

    @Test
    void toDtosByViews_shouldSortDescendingAndSlice() {
        when(statsService.getViews(any(LocalDateTime.class), any(LocalDateTime.class), anyList(), eq(true)))
                .thenReturn(List.of(
                        new ViewStatsDto("ewm-main-service", "/events/1", 1L),
                        new ViewStatsDto("ewm-main-service", "/events/2", 5L),
                        new ViewStatsDto("ewm-main-service", "/events/3", 3L)));
        stubNoConfirmedRequests();

        List<EventDto> dtos = eventEnricher.toDtosByViews(
                List.of(buildEvent(1L), buildEvent(2L), buildEvent(3L)), new PageParams(1, 1));

        assertThat(dtos).extracting(EventDto::getId).containsExactly(3L);
    }

    @Test
    void toDtosByViews_shouldReturnEmptyWhenOffsetIsTooBig() {
        List<EventDto> dtos = eventEnricher.toDtosByViews(List.of(), new PageParams(10, 10));

        assertThat(dtos).isEmpty();
    }

    @Test
    void toDtos_shouldCountConfirmedRequestsInSingleBatch() {
        when(statsService.getViews(any(LocalDateTime.class), any(LocalDateTime.class), anyList(), anyBoolean()))
                .thenReturn(List.of());
        when(requestService.getEventRequestsByStatus(anyList(), eq(RequestStatus.CONFIRMED)))
                .thenReturn(List.of(buildConfirmedRequest(1L), buildConfirmedRequest(1L)));

        List<EventDto> dtos = eventEnricher.toDtos(List.of(buildEvent(1L), buildEvent(2L), buildEvent(3L)));

        assertThat(dtos).extracting(EventDto::getConfirmedRequests).containsExactly(2L, 0L, 0L);
        verify(requestService).getEventRequestsByStatus(List.of(1L, 2L, 3L), RequestStatus.CONFIRMED);
    }

    private void stubNoConfirmedRequests() {
        when(requestService.getEventRequestsByStatus(anyList(), eq(RequestStatus.CONFIRMED)))
                .thenReturn(List.of());
    }

    private Request buildConfirmedRequest(Long eventId) {
        return Request.builder()
                .event(Event.builder().id(eventId).build())
                .status(RequestStatus.CONFIRMED)
                .build();
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
