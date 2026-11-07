package ru.practicum.request.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.event.model.Event;
import ru.practicum.event.service.EventRequestService;
import ru.practicum.request.dto.RequestDto;
import ru.practicum.request.dto.RequestStatusUpdateResult;
import ru.practicum.request.mapper.RequestMapper;
import ru.practicum.request.model.Request;
import ru.practicum.request.model.RequestStatus;
import ru.practicum.user.model.User;
import ru.practicum.utils.error.exception.BadRequestException;
import ru.practicum.utils.error.exception.ConflictException;
import ru.practicum.utils.error.exception.NotFoundException;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EventRequestController.class)
@Import(RequestMapper.class)
class EventRequestControllerTest {

    private static final String PATH = "/users/{userId}/events/{eventId}/requests";
    private static final Long OWNER_ID = 1L;
    private static final Long EVENT_ID = 10L;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EventRequestService eventRequestService;

    @Test
    void getEventRequests_shouldReturn200AndList() throws Exception {
        when(eventRequestService.getEventRequests(OWNER_ID, EVENT_ID))
                .thenReturn(List.of(buildRequest(1L, RequestStatus.PENDING)));

        mockMvc.perform(get(PATH, OWNER_ID, EVENT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].event").value(EVENT_ID))
                .andExpect(jsonPath("$[0].requester").value(101L))
                .andExpect(jsonPath("$[0].status").value("PENDING"))
                .andExpect(jsonPath("$[0].created").isNotEmpty());
    }

    @Test
    void getEventRequests_whenEventNotFound_shouldReturn404() throws Exception {
        when(eventRequestService.getEventRequests(OWNER_ID, EVENT_ID))
                .thenThrow(new NotFoundException("Event with id=10 was not found"));

        mockMvc.perform(get(PATH, OWNER_ID, EVENT_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    void changeRequestStatus_whenConfirmed_shouldReturn200WithConfirmedRequests() throws Exception {
        when(eventRequestService.changeRequestStatus(eq(OWNER_ID), eq(EVENT_ID), any()))
                .thenReturn(new RequestStatusUpdateResult(
                        List.of(buildDto(1L, RequestStatus.CONFIRMED)), List.of()));

        mockMvc.perform(patch(PATH, OWNER_ID, EVENT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"requestIds\": [1], \"status\": \"CONFIRMED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.confirmedRequests.length()").value(1))
                .andExpect(jsonPath("$.confirmedRequests[0].id").value(1L))
                .andExpect(jsonPath("$.confirmedRequests[0].status").value("CONFIRMED"))
                .andExpect(jsonPath("$.confirmedRequests[0].event").value(EVENT_ID))
                .andExpect(jsonPath("$.confirmedRequests[0].requester").value(101L))
                .andExpect(jsonPath("$.rejectedRequests.length()").value(0));

        verify(eventRequestService).changeRequestStatus(eq(OWNER_ID), eq(EVENT_ID), any());
    }

    @Test
    void changeRequestStatus_whenRejected_shouldReturn200WithRejectedRequests() throws Exception {
        when(eventRequestService.changeRequestStatus(eq(OWNER_ID), eq(EVENT_ID), any()))
                .thenReturn(new RequestStatusUpdateResult(
                        List.of(), List.of(buildDto(1L, RequestStatus.REJECTED))));

        mockMvc.perform(patch(PATH, OWNER_ID, EVENT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"requestIds\": [1], \"status\": \"REJECTED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.confirmedRequests.length()").value(0))
                .andExpect(jsonPath("$.rejectedRequests.length()").value(1))
                .andExpect(jsonPath("$.rejectedRequests[0].status").value("REJECTED"));
    }

    @Test
    void changeRequestStatus_whenStatusIsNotSupported_shouldReturn400() throws Exception {
        when(eventRequestService.changeRequestStatus(eq(OWNER_ID), eq(EVENT_ID), any()))
                .thenThrow(new BadRequestException("Request status must be CONFIRMED or REJECTED"));

        mockMvc.perform(patch(PATH, OWNER_ID, EVENT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"requestIds\": [1], \"status\": \"PENDING\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void changeRequestStatus_whenRequestIdsAreEmpty_shouldReturn400() throws Exception {
        mockMvc.perform(patch(PATH, OWNER_ID, EVENT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"requestIds\": [], \"status\": \"CONFIRMED\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void changeRequestStatus_whenLimitReached_shouldReturn409() throws Exception {
        when(eventRequestService.changeRequestStatus(eq(OWNER_ID), eq(EVENT_ID), any()))
                .thenThrow(new ConflictException("The participant limit has been reached"));

        mockMvc.perform(patch(PATH, OWNER_ID, EVENT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"requestIds\": [1], \"status\": \"CONFIRMED\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void changeRequestStatus_whenEventNotFound_shouldReturn404() throws Exception {
        when(eventRequestService.changeRequestStatus(eq(OWNER_ID), eq(EVENT_ID), any()))
                .thenThrow(new NotFoundException("Event with id=10 was not found"));

        mockMvc.perform(patch(PATH, OWNER_ID, EVENT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"requestIds\": [1], \"status\": \"CONFIRMED\"}"))
                .andExpect(status().isNotFound());
    }

    private RequestDto buildDto(Long id, RequestStatus status) {
        return RequestDto.builder()
                .id(id)
                .event(EVENT_ID)
                .requester(100L + id)
                .created(LocalDateTime.of(2024, 1, 1, 12, 0))
                .status(status)
                .build();
    }

    private Request buildRequest(Long id, RequestStatus status) {
        return Request.builder()
                .id(id)
                .event(Event.builder().id(EVENT_ID).build())
                .requester(User.builder().id(100L + id).build())
                .created(LocalDateTime.of(2024, 1, 1, 12, 0))
                .status(status)
                .build();
    }
}
