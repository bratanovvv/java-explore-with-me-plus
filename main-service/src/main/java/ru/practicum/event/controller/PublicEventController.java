package ru.practicum.event.controller;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.event.dto.query.EventSort;
import ru.practicum.event.dto.query.PublicEventFilter;
import ru.practicum.event.dto.response.EventDto;
import ru.practicum.event.mapper.EventEnricher;
import ru.practicum.event.model.Event;
import ru.practicum.event.service.EventService;
import ru.practicum.utils.integrations.StatsService;
import ru.practicum.utils.params.PageParams;

import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping("/events")
public class PublicEventController {

    private final EventService eventService;
    private final EventEnricher eventEnricher;
    private final StatsService statsService;

    public PublicEventController(EventService eventService,
                                 EventEnricher eventEnricher,
                                 StatsService statsService) {
        this.eventService = eventService;
        this.eventEnricher = eventEnricher;
        this.statsService = statsService;
    }

    @GetMapping
    @JsonView(EventDto.ShortEvent.class)
    public List<EventDto> getEvents(
            @Valid @ModelAttribute PublicEventFilter filter,
            @RequestParam(defaultValue = PageParams.DEFAULT_FROM) @PositiveOrZero int from,
            @RequestParam(defaultValue = PageParams.DEFAULT_SIZE) @Positive int size,
            HttpServletRequest request) {
        statsService.hit(request);
        PageParams pageParams = new PageParams(from, size);
        List<Event> events = eventService.getPublicEvents(filter, pageParams);
        if (filter.sort() == EventSort.VIEWS) {
            return eventEnricher.toDtosByViews(events, pageParams);
        }
        return eventEnricher.toDtos(events);
    }

    @GetMapping("/{id}")
    @JsonView(EventDto.FullEvent.class)
    public EventDto getEvent(@PathVariable Long id, HttpServletRequest request) {
        statsService.hit(request);
        return eventEnricher.toDto(eventService.getPublicEvent(id));
    }
}
