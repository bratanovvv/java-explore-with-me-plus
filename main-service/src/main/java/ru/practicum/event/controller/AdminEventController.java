package ru.practicum.event.controller;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.event.dto.query.AdminEventFilter;
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
@RequestMapping("/admin/events")
public class AdminEventController {

    private final EventService eventService;
    private final EventMapper eventMapper;
    private final EventEnricher eventEnricher;

    public AdminEventController(EventService eventService,
                                EventMapper eventMapper,
                                EventEnricher eventEnricher) {
        this.eventService = eventService;
        this.eventMapper = eventMapper;
        this.eventEnricher = eventEnricher;
    }

    @GetMapping
    @JsonView(EventDto.FullEvent.class)
    public List<EventDto> getEvents(
            @ModelAttribute AdminEventFilter filter,
            @RequestParam(defaultValue = PageParams.DEFAULT_FROM) @PositiveOrZero int from,
            @RequestParam(defaultValue = PageParams.DEFAULT_SIZE) @Positive int size) {
        return eventEnricher.toDtos(eventService.getAdminEvents(filter, new PageParams(from, size)));
    }

    @PatchMapping("/{eventId}")
    @JsonView(EventDto.FullEvent.class)
    public EventDto update(@PathVariable Long eventId,
                           @Valid @RequestBody EventRequestDto request) {
        Event updates = eventMapper.toEntity(request);
        Event event = eventService.updateEventByAdmin(eventId, updates, request.getStateAction());
        return eventEnricher.toDto(event);
    }
}
