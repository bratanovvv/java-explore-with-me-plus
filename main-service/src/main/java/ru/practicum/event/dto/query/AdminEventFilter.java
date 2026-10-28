package ru.practicum.event.dto.query;

import ru.practicum.event.model.EventState;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Критерии административного поиска событий (query-параметры {@code GET /admin/events}).
 */
public record AdminEventFilter(
        List<Long> users,
        List<EventState> states,
        List<Long> categories,
        LocalDateTime rangeStart,
        LocalDateTime rangeEnd) {
}
