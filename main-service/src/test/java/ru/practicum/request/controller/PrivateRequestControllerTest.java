package ru.practicum.request.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.event.model.Event;
import ru.practicum.request.mapper.RequestMapper;
import ru.practicum.request.model.Request;
import ru.practicum.request.model.RequestStatus;
import ru.practicum.request.service.RequestService;
import ru.practicum.user.model.User;
import ru.practicum.utils.error.exception.ConflictException;
import ru.practicum.utils.error.exception.NotFoundException;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PrivateRequestController.class)
@Import(RequestMapper.class)
class PrivateRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RequestService requestService;

    private Request createTestRequest() {
        User user = User.builder().id(2L).build();
        Event event = Event.builder().id(10L).build();
        return Request.builder()
                .id(1L)
                .event(event)
                .requester(user)
                .created(LocalDateTime.of(2024, 1, 1, 12, 0))
                .status(RequestStatus.PENDING)
                .build();
    }

    @Test
    void getUserRequests_shouldReturn200AndList() throws Exception {
        Long userId = 2L;
        Request request = createTestRequest();
        when(requestService.getUserRequests(userId)).thenReturn(List.of(request));

        mockMvc.perform(get("/users/{userId}/requests", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].event").value(10L))
                .andExpect(jsonPath("$[0].requester").value(2L))
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    void getUserRequests_whenUserNotFound_shouldReturn404() throws Exception {
        Long userId = 99L;
        when(requestService.getUserRequests(userId))
                .thenThrow(new NotFoundException("User with id=99 was not found"));

        mockMvc.perform(get("/users/{userId}/requests", userId))
                .andExpect(status().isNotFound());
    }

    @Test
    void createRequest_shouldReturn201AndCreatedRequest() throws Exception {
        Long userId = 2L;
        Long eventId = 10L;
        Request request = createTestRequest();

        when(requestService.createRequest(userId, eventId)).thenReturn(request);

        mockMvc.perform(post("/users/{userId}/requests", userId)
                        .param("eventId", eventId.toString()))
                .andExpect(status().isCreated()) // Проверяем, что вернулся 201
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.event").value(10L))
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(requestService).createRequest(userId, eventId);
    }

    @Test
    void createRequest_whenEventNotFound_shouldReturn404() throws Exception {
        Long userId = 2L;
        Long eventId = 99L;
        when(requestService.createRequest(userId, eventId))
                .thenThrow(new NotFoundException("Event with id=99 was not found"));

        mockMvc.perform(post("/users/{userId}/requests", userId)
                        .param("eventId", eventId.toString()))
                .andExpect(status().isNotFound());
    }

    @Test
    void createRequest_whenConflict_shouldReturn409() throws Exception {
        Long userId = 2L;
        Long eventId = 10L;
        when(requestService.createRequest(userId, eventId))
                .thenThrow(new ConflictException("Request already exists"));

        mockMvc.perform(post("/users/{userId}/requests", userId)
                        .param("eventId", eventId.toString()))
                .andExpect(status().isConflict());
    }

    @Test
    void createRequest_withoutEventIdParam_shouldReturn400() throws Exception {
        // Проверяем, что если забыть передать eventId, Spring не упадет с 500, а вернет 400
        mockMvc.perform(post("/users/{userId}/requests", 2L))
                .andExpect(status().isBadRequest());
    }

    @Test
    void cancelRequest_shouldReturn200AndCanceledRequest() throws Exception {
        Long userId = 2L;
        Long requestId = 1L;

        Request canceledRequest = createTestRequest();
        canceledRequest.setStatus(RequestStatus.CANCELED); // Меняем статус для теста

        when(requestService.cancelRequest(userId, requestId)).thenReturn(canceledRequest);

        mockMvc.perform(patch("/users/{userId}/requests/{requestId}/cancel", userId, requestId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("CANCELED")); // Проверяем, что статус изменился в JSON
    }

    @Test
    void cancelRequest_whenNotFound_shouldReturn404() throws Exception {
        Long userId = 2L;
        Long requestId = 99L;
        when(requestService.cancelRequest(userId, requestId))
                .thenThrow(new NotFoundException("Request with id=99 was not found"));

        mockMvc.perform(patch("/users/{userId}/requests/{requestId}/cancel", userId, requestId))
                .andExpect(status().isNotFound());
    }

    @Test
    void cancelRequest_whenConflict_shouldReturn409() throws Exception {
        Long userId = 2L;
        Long requestId = 1L;
        when(requestService.cancelRequest(userId, requestId))
                .thenThrow(new ConflictException("Only pending requests can be canceled"));

        mockMvc.perform(patch("/users/{userId}/requests/{requestId}/cancel", userId, requestId))
                .andExpect(status().isConflict());
    }
}
