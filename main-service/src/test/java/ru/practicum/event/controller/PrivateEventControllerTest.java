package ru.practicum.event.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.event.dto.common.LocationDto;
import ru.practicum.event.dto.request.EventRequestDto;
import ru.practicum.event.dto.request.EventStateAction;
import ru.practicum.event.dto.response.EventDto;
import ru.practicum.event.mapper.EventEnricher;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventState;
import ru.practicum.event.service.EventService;
import ru.practicum.user.dto.UserDto;
import ru.practicum.utils.params.PageParams;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PrivateEventController.class)
class PrivateEventControllerTest {

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
    void create_shouldReturnFullViewAndApplyDefaults() throws Exception {
        Event event = Event.builder().build();
        when(eventMapper.toEntity(any(EventRequestDto.class))).thenReturn(event);
        when(eventService.createEvent(1L, event)).thenReturn(event);
        when(eventEnricher.toDto(event)).thenReturn(buildDto(EventState.PENDING));

        mockMvc.perform(post("/users/1/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validEventBody())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.state").value("PENDING"))
                .andExpect(jsonPath("$.description").value("Описание нового события"))
                .andExpect(jsonPath("$.initiator.email").doesNotExist());

        verify(eventMapper).toEntity(any(EventRequestDto.class));
        verify(eventService).createEvent(1L, event);
    }

    @Test
    void create_shouldRejectTooShortDescription() throws Exception {
        Map<String, Object> body = validEventBody();
        body.put("description", "short");

        mockMvc.perform(post("/users/1/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"));

        verify(eventService, never()).createEvent(any(), any());
    }

    @Test
    void create_shouldRejectMissingDescription() throws Exception {
        Map<String, Object> body = validEventBody();
        body.remove("description");

        mockMvc.perform(post("/users/1/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());

        verify(eventService, never()).createEvent(any(), any());
    }

    @Test
    void create_shouldRejectNegativeParticipantLimit() throws Exception {
        Map<String, Object> body = validEventBody();
        body.put("participantLimit", -6);

        mockMvc.perform(post("/users/1/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());

        verify(eventService, never()).createEvent(any(), any());
    }

    @Test
    void update_shouldRejectTooShortTitle() throws Exception {
        mockMvc.perform(patch("/users/1/events/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"ab\"}"))
                .andExpect(status().isBadRequest());

        verify(eventService, never()).updateEventByUser(any(), any(), any(), any());
    }

    @Test
    void update_shouldPassPatchAndStateAction() throws Exception {
        Event patch = Event.builder().title("Новое название").build();
        Event event = Event.builder().id(10L).build();
        when(eventMapper.toEntity(any(EventRequestDto.class))).thenReturn(patch);
        when(eventService.updateEventByUser(1L, 10L, patch, EventStateAction.CANCEL_REVIEW)).thenReturn(event);
        when(eventEnricher.toDto(event)).thenReturn(buildDto(EventState.CANCELED));

        mockMvc.perform(patch("/users/1/events/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"stateAction\":\"CANCEL_REVIEW\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.state").value("CANCELED"));

        verify(eventService).updateEventByUser(1L, 10L, patch, EventStateAction.CANCEL_REVIEW);
    }

    @Test
    void getEvents_shouldUseDefaultPaginationAndShortView() throws Exception {
        Event event = Event.builder().id(1L).build();
        EventDto dto = buildDto(EventState.PENDING);
        dto.setId(1L);
        when(eventService.getUserEvents(1L, new PageParams(0, 10))).thenReturn(List.of(event));
        when(eventEnricher.toDtos(List.of(event))).thenReturn(List.of(dto));

        mockMvc.perform(get("/users/1/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].description").doesNotExist())
                .andExpect(jsonPath("$[0].state").doesNotExist())
                .andExpect(jsonPath("$[0].initiator.email").doesNotExist());

        verify(eventService).getUserEvents(1L, new PageParams(0, 10));
    }

    private Map<String, Object> validEventBody() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("annotation", "Аннотация нового события");
        body.put("category", 2);
        body.put("description", "Описание нового события");
        body.put("eventDate", "2030-01-01 12:00:00");
        body.put("location", Map.of("lat", 55.75, "lon", 37.62));
        body.put("title", "Событие");
        return body;
    }

    private EventDto buildDto(EventState state) {
        return EventDto.builder()
                .id(10L)
                .title("Событие")
                .annotation("Аннотация нового события")
                .description("Описание нового события")
                .category(CategoryDto.builder().id(2L).name("Концерты").build())
                .paid(false)
                .participantLimit(0)
                .requestModeration(true)
                .state(state)
                .eventDate(LocalDateTime.now().plusDays(1))
                .createdOn(LocalDateTime.now())
                .initiator(new UserDto(1L, "Иван", "ivan@example.com"))
                .views(0L)
                .confirmedRequests(0L)
                .location(new LocationDto(55.75, 37.62))
                .build();
    }
}
