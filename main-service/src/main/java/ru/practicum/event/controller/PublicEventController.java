package ru.practicum.event.controller;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
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
import ru.practicum.utils.formats.DateFormats;
import ru.practicum.utils.integrations.StatsService;
import ru.practicum.utils.params.PageParams;

import java.time.LocalDateTime;
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
            @RequestParam(required = false) @Size(min = 1, max = 7000) String text,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) Boolean paid,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = DateFormats.DATE_TIME) LocalDateTime rangeStart,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = DateFormats.DATE_TIME) LocalDateTime rangeEnd,
            @RequestParam(defaultValue = "false") boolean onlyAvailable,
            @RequestParam(required = false) EventSort sort,
            @RequestParam(defaultValue = PageParams.DEFAULT_FROM) @PositiveOrZero int from,
            @RequestParam(defaultValue = PageParams.DEFAULT_SIZE) @Positive int size,
            HttpServletRequest request) {
        statsService.hit(request);
        PublicEventFilter filter = new PublicEventFilter(
                text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort);
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
