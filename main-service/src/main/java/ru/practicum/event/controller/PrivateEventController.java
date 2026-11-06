package ru.practicum.event.controller;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.groups.Default;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.event.dto.request.EventRequestDto;
import ru.practicum.event.dto.response.EventDto;
import ru.practicum.event.mapper.EventEnricher;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.model.Event;
import ru.practicum.event.service.EventService;
import ru.practicum.utils.params.PageParams;

import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping("/users/{userId}/events")
public class PrivateEventController {

    private final EventService eventService;
    private final EventMapper eventMapper;
    private final EventEnricher eventEnricher;

    public PrivateEventController(EventService eventService,
                                  EventMapper eventMapper,
                                  EventEnricher eventEnricher) {
        this.eventService = eventService;
        this.eventMapper = eventMapper;
        this.eventEnricher = eventEnricher;
    }

    @GetMapping
    @JsonView(EventDto.ShortEvent.class)
    public List<EventDto> getEvents(
            @PathVariable Long userId,
            @RequestParam(defaultValue = PageParams.DEFAULT_FROM) @PositiveOrZero int from,
            @RequestParam(defaultValue = PageParams.DEFAULT_SIZE) @Positive int size) {
        return eventEnricher.toDtos(eventService.getUserEvents(userId, new PageParams(from, size)));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @JsonView(EventDto.FullEvent.class)
    public EventDto create(
            @PathVariable Long userId,
            @Validated({Default.class, EventRequestDto.Create.class}) @RequestBody EventRequestDto request) {
        Event event = eventMapper.toEntity(request);
        return eventEnricher.toDto(eventService.createEvent(userId, event));
    }

    @GetMapping("/{eventId}")
    @JsonView(EventDto.FullEvent.class)
    public EventDto getEvent(@PathVariable Long userId, @PathVariable Long eventId) {
        return eventEnricher.toDto(eventService.getUserEvent(userId, eventId));
    }

    @PatchMapping("/{eventId}")
    @JsonView(EventDto.FullEvent.class)
    public EventDto update(@PathVariable Long userId,
                           @PathVariable Long eventId,
                           @Valid @RequestBody EventRequestDto request) {
        Event updates = eventMapper.toEntity(request);
        Event event = eventService.updateEventByUser(userId, eventId, updates, request.getStateAction());
        return eventEnricher.toDto(event);
    }
}
