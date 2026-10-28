package ru.practicum.event.dto.query;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Критерии публичного поиска событий (query-параметры {@code GET /events}).
 */
public record PublicEventFilter(
        String text,
        List<Long> categories,
        Boolean paid,
        LocalDateTime rangeStart,
        LocalDateTime rangeEnd,
        boolean onlyAvailable,
        EventSort sort) {
}
