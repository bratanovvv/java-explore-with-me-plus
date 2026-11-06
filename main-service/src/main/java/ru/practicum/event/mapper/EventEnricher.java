package ru.practicum.event.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.event.dto.response.EventDto;
import ru.practicum.event.model.Event;
import ru.practicum.request.model.RequestStatus;
import ru.practicum.request.service.RequestService;
import ru.practicum.utils.integrations.StatsEnricher;
import ru.practicum.utils.integrations.StatsService;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Обогащение DTO события: просмотры из сервиса статистики и количество подтверждённых заявок из сервиса заявок.
 */
@Component
public class EventEnricher extends StatsEnricher<Event, EventDto> {

    private static final String EVENT_URI_PREFIX = "/events/";

    private final EventMapper eventMapper;
    private final RequestService requestService;

    public EventEnricher(EventMapper eventMapper, StatsService statsService, RequestService requestService) {
        super(statsService);
        this.eventMapper = eventMapper;
        this.requestService = requestService;
    }

    @Override
    public List<EventDto> toDtos(List<Event> events) {
        List<EventDto> dtos = super.toDtos(events);
        if (dtos.isEmpty()) {
            return dtos;
        }
        List<Long> eventIds = dtos.stream().map(EventDto::getId).toList();
        Map<Long, Long> confirmedRequests = requestService
                .getEventRequestsByStatus(eventIds, RequestStatus.CONFIRMED).stream()
                .collect(Collectors.groupingBy(request -> request.getEvent().getId(), Collectors.counting()));
        dtos.forEach(dto -> dto.setConfirmedRequests(confirmedRequests.getOrDefault(dto.getId(), 0L)));
        return dtos;
    }

    @Override
    protected EventDto map(Event event) {
        return eventMapper.toDto(event);
    }

    @Override
    protected String uri(Event event) {
        return EVENT_URI_PREFIX + event.getId();
    }

    @Override
    protected void applyViews(EventDto dto, long views) {
        dto.setViews(views);
    }

    @Override
    protected Comparator<EventDto> viewsComparator() {
        return Comparator.comparing(EventDto::getViews).reversed()
                .thenComparing(EventDto::getId);
    }
}
