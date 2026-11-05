package ru.practicum.request.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.event.service.EventRequestService;
import ru.practicum.request.dto.RequestDto;
import ru.practicum.request.dto.RequestStatusUpdate;
import ru.practicum.request.dto.RequestStatusUpdateResult;
import ru.practicum.request.mapper.RequestMapper;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}/events/{eventId}/requests")
public class EventRequestController {

    private final EventRequestService eventRequestService;
    private final RequestMapper requestMapper;

    public EventRequestController(EventRequestService eventRequestService, RequestMapper requestMapper) {
        this.eventRequestService = eventRequestService;
        this.requestMapper = requestMapper;
    }

    @GetMapping
    public List<RequestDto> getEventRequests(@PathVariable Long userId, @PathVariable Long eventId) {
        return requestMapper.toDtoList(eventRequestService.getEventRequests(userId, eventId));
    }

    @PatchMapping
    public RequestStatusUpdateResult changeRequestStatus(
            @PathVariable Long userId,
            @PathVariable Long eventId,
            @Valid @RequestBody RequestStatusUpdate update) {
        return eventRequestService.changeRequestStatus(userId, eventId, update);
    }
}
