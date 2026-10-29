package ru.practicum.event.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.event.dto.query.AdminEventFilter;
import ru.practicum.event.dto.request.EventRequestDto;
import ru.practicum.event.dto.request.EventStateAction;
import ru.practicum.event.dto.response.EventDto;
import ru.practicum.event.mapper.EventEnricher;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventState;
import ru.practicum.event.service.EventService;
import ru.practicum.utils.error.exception.BadRequestException;
import ru.practicum.utils.error.exception.ConflictException;
import ru.practicum.utils.params.PageParams;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminEventController.class)
class AdminEventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EventService eventService;

    @MockBean
    private EventMapper eventMapper;

    @MockBean
    private EventEnricher eventEnricher;

    @Test
    void getEvents_shouldPassFiltersAndReturnFullView() throws Exception {
        Event event = Event.builder().id(1L).build();
        when(eventService.getAdminEvents(any(AdminEventFilter.class), any(PageParams.class))).thenReturn(List.of(event));
        when(eventEnricher.toDtos(List.of(event)))
                .thenReturn(List.of(EventDto.builder()
                        .id(1L)
                        .state(EventState.PUBLISHED)
                        .description("Описание события")
                        .build()));

        mockMvc.perform(get("/admin/events")
                        .param("users", "1")
                        .param("states", "PUBLISHED")
                        .param("categories", "2")
                        .param("rangeStart", "2022-01-06 13:30:38")
                        .param("rangeEnd", "2097-09-06 13:30:38")
                        .param("from", "0")
                        .param("size", "1000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].state").value("PUBLISHED"))
                .andExpect(jsonPath("$[0].description").value("Описание события"));

        verify(eventService).getAdminEvents(
                eq(new AdminEventFilter(
                        List.of(1L),
                        List.of(EventState.PUBLISHED),
                        List.of(2L),
                        LocalDateTime.of(2022, 1, 6, 13, 30, 38),
                        LocalDateTime.of(2097, 9, 6, 13, 30, 38))),
                eq(new PageParams(0, 1000)));
    }

    @Test
    void getEvents_shouldRejectInvertedRange() throws Exception {
        when(eventService.getAdminEvents(any(), any()))
                .thenThrow(new BadRequestException("rangeStart must be before or equal to rangeEnd"));

        mockMvc.perform(get("/admin/events")
                        .param("rangeStart", "2097-09-06 13:30:38")
                        .param("rangeEnd", "2022-01-06 13:30:38"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"));
    }

    @Test
    void update_shouldPassPatchAndStateAction() throws Exception {
        Event patch = Event.builder().build();
        Event event = Event.builder().id(1L).state(EventState.PUBLISHED).build();
        when(eventMapper.toEntity(any(EventRequestDto.class))).thenReturn(patch);
        when(eventService.updateEventByAdmin(1L, patch, EventStateAction.PUBLISH_EVENT)).thenReturn(event);
        when(eventEnricher.toDto(event)).thenReturn(EventDto.builder().id(1L).build());

        mockMvc.perform(patch("/admin/events/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("stateAction", "PUBLISH_EVENT"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(eventService).updateEventByAdmin(1L, patch, EventStateAction.PUBLISH_EVENT);
    }

    @Test
    void update_shouldReturnConflictForWrongState() throws Exception {
        when(eventMapper.toEntity(any(EventRequestDto.class))).thenReturn(Event.builder().build());
        when(eventService.updateEventByAdmin(eq(1L), any(Event.class), eq(EventStateAction.PUBLISH_EVENT)))
                .thenThrow(new ConflictException("Cannot publish the event because it's not in the right state"));

        mockMvc.perform(patch("/admin/events/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("stateAction", "PUBLISH_EVENT"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value("CONFLICT"));
    }

    @Test
    void update_shouldRejectTooShortTitle() throws Exception {
        mockMvc.perform(patch("/admin/events/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"ab\"}"))
                .andExpect(status().isBadRequest());

        verify(eventService, never()).updateEventByAdmin(any(), any(), any());
    }
}
