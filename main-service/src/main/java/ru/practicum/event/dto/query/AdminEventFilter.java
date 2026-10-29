package ru.practicum.event.dto.query;

import org.springframework.format.annotation.DateTimeFormat;
import ru.practicum.event.model.EventState;
import ru.practicum.utils.formats.DateFormats;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Критерии административного поиска событий (query-параметры {@code GET /admin/events}).
 */
public record AdminEventFilter(
        List<Long> users,
        List<EventState> states,
        List<Long> categories,
        @DateTimeFormat(pattern = DateFormats.DATE_TIME) LocalDateTime rangeStart,
        @DateTimeFormat(pattern = DateFormats.DATE_TIME) LocalDateTime rangeEnd) {
}
