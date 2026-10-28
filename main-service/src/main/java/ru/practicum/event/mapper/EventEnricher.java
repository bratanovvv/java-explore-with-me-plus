package ru.practicum.event.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.event.dto.response.EventDto;
import ru.practicum.event.model.Event;
import ru.practicum.utils.integrations.StatsEnricher;
import ru.practicum.utils.integrations.StatsService;

import java.util.Comparator;

/**
 * Обогащение DTO события просмотрами из сервиса статистики.
 * TODO: обогащать {@code confirmedRequests} батчем {@code group by event_id} по статусу {@code CONFIRMED} (см. {@code ParticipationRequest}). Пока заявок нет — всегда 0, что корректно.
 */
@Component
public class EventEnricher extends StatsEnricher<Event, EventDto> {

    private static final String EVENT_URI_PREFIX = "/events/";

    private final EventMapper eventMapper;

    public EventEnricher(EventMapper eventMapper, StatsService statsService) {
        super(statsService);
        this.eventMapper = eventMapper;
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
