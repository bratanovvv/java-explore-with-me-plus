package ru.practicum.request.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.request.dto.RequestDto;
import ru.practicum.request.mapper.RequestMapper;
import ru.practicum.request.service.RequestService;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}/requests")
public class PrivateRequestController {

    private final RequestService requestService;
    private final RequestMapper requestMapper;

    public PrivateRequestController(RequestService requestService, RequestMapper requestMapper) {
        this.requestService = requestService;
        this.requestMapper = requestMapper;
    }

    @GetMapping
    public List<RequestDto> getUserRequests(@PathVariable Long userId) {
        return requestMapper.toDtoList(requestService.getUserRequests(userId));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RequestDto createRequest(
            @PathVariable Long userId,
            @RequestParam Long eventId) {
        return requestMapper.toDto(requestService.createRequest(userId, eventId));
    }

    @PatchMapping("/{requestId}/cancel")
    public RequestDto cancelRequest(
            @PathVariable Long userId,
            @PathVariable Long requestId) {
        return requestMapper.toDto(requestService.cancelRequest(userId, requestId));
    }
}
