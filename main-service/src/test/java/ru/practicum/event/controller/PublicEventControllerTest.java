package ru.practicum.event.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.event.dto.common.LocationDto;
import ru.practicum.event.dto.query.EventSort;
import ru.practicum.event.dto.query.PublicEventFilter;
import ru.practicum.event.dto.response.EventDto;
import ru.practicum.event.mapper.EventEnricher;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventState;
import ru.practicum.event.service.EventService;
import ru.practicum.user.dto.UserDto;
import ru.practicum.utils.error.exception.NotFoundException;
import ru.practicum.utils.integrations.StatsService;
import ru.practicum.utils.params.PageParams;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PublicEventController.class)
class PublicEventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EventService eventService;

    @MockBean
    private EventEnricher eventEnricher;

    @MockBean
    private StatsService statsService;

    @Test
    void getEvents_shouldReturnShortViewAndBuildFilter() throws Exception {
        Event event = Event.builder().id(1L).build();
        when(eventService.getPublicEvents(any(PublicEventFilter.class), any(PageParams.class)))
                .thenReturn(List.of(event));
        when(eventEnricher.toDtos(List.of(event))).thenReturn(List.of(buildDto()));

        mockMvc.perform(get("/events")
                        .param("categories", "2")
                        .param("paid", "true")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Событие"))
                .andExpect(jsonPath("$[0].views").value(5))
                .andExpect(jsonPath("$[0].confirmedRequests").value(0))
                .andExpect(jsonPath("$[0].description").doesNotExist())
                .andExpect(jsonPath("$[0].state").doesNotExist())
                .andExpect(jsonPath("$[0].location").doesNotExist())
                .andExpect(jsonPath("$[0].initiator.email").doesNotExist());

        verify(statsService, times(1)).hit(any(HttpServletRequest.class));
        ArgumentCaptor<PublicEventFilter> filterCaptor = ArgumentCaptor.forClass(PublicEventFilter.class);
        verify(eventService).getPublicEvents(filterCaptor.capture(), eq(new PageParams(0, 10)));
        assertThat(filterCaptor.getValue().categories()).containsExactly(2L);
        assertThat(filterCaptor.getValue().paid()).isTrue();
        assertThat(filterCaptor.getValue().sort()).isNull();
    }

    @Test
    void getEvents_shouldDelegateViewsSorting() throws Exception {
        Event event = Event.builder().id(1L).build();
        when(eventService.getPublicEvents(any(PublicEventFilter.class), any(PageParams.class)))
                .thenReturn(List.of(event));
        when(eventEnricher.toDtosByViews(List.of(event), new PageParams(0, 10)))
                .thenReturn(List.of(buildDto()));

        mockMvc.perform(get("/events").param("sort", "VIEWS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].description").doesNotExist());

        ArgumentCaptor<PublicEventFilter> filterCaptor = ArgumentCaptor.forClass(PublicEventFilter.class);
        verify(eventService).getPublicEvents(filterCaptor.capture(), eq(new PageParams(0, 10)));
        assertThat(filterCaptor.getValue().sort()).isEqualTo(EventSort.VIEWS);
        verify(eventEnricher).toDtosByViews(List.of(event), new PageParams(0, 10));
    }

    @Test
    void getEvent_shouldReturnFullViewAndWriteSingleHit() throws Exception {
        Event event = Event.builder().id(1L).build();
        when(eventService.getPublicEvent(1L)).thenReturn(event);
        when(eventEnricher.toDto(event)).thenReturn(buildDto());

        mockMvc.perform(get("/events/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.state").value("PUBLISHED"))
                .andExpect(jsonPath("$.description").value("Описание события"))
                .andExpect(jsonPath("$.location.lat").value(55.75))
                .andExpect(jsonPath("$.initiator.id").value(3))
                .andExpect(jsonPath("$.initiator.name").value("Иван"))
                .andExpect(jsonPath("$.initiator.email").doesNotExist());

        verify(statsService, times(1)).hit(any(HttpServletRequest.class));
    }

    @Test
    void getEvent_shouldReturnNotFoundForUnpublishedEvent() throws Exception {
        when(eventService.getPublicEvent(99L))
                .thenThrow(new NotFoundException("Event with id=99 was not found"));

        mockMvc.perform(get("/events/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Event with id=99 was not found"));
    }

    @Test
    void getEvents_shouldRejectNegativeFrom() throws Exception {
        mockMvc.perform(get("/events").param("from", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"));

        verify(eventService, never()).getPublicEvents(any(), any());
    }

    @Test
    void getEvents_shouldRejectUnknownSort() throws Exception {
        mockMvc.perform(get("/events").param("sort", "WRONG"))
                .andExpect(status().isBadRequest());
    }

    private EventDto buildDto() {
        return EventDto.builder()
                .id(1L)
                .title("Событие")
                .annotation("Аннотация события")
                .description("Описание события")
                .category(CategoryDto.builder().id(2L).name("Концерты").build())
                .paid(false)
                .eventDate(LocalDateTime.now().plusDays(1))
                .initiator(new UserDto(3L, "Иван", "ivan@example.com"))
                .views(5L)
                .confirmedRequests(0L)
                .participantLimit(0)
                .state(EventState.PUBLISHED)
                .createdOn(LocalDateTime.now())
                .publishedOn(LocalDateTime.now())
                .location(new LocationDto(55.75, 37.62))
                .requestModeration(true)
                .build();
    }
}
