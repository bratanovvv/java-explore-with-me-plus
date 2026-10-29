package ru.practicum.event.dto.query;

import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;
import ru.practicum.utils.formats.DateFormats;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Критерии публичного поиска событий (query-параметры {@code GET /events}).
 */
public record PublicEventFilter(
        @Size(min = 1, max = 7000) String text,
        List<Long> categories,
        Boolean paid,
        @DateTimeFormat(pattern = DateFormats.DATE_TIME) LocalDateTime rangeStart,
        @DateTimeFormat(pattern = DateFormats.DATE_TIME) LocalDateTime rangeEnd,
        Boolean onlyAvailable,
        EventSort sort) {

    public PublicEventFilter {
        if (rangeStart == null && rangeEnd == null) {
            rangeStart = LocalDateTime.now();
        }
        if (onlyAvailable == null) {
            onlyAvailable = false;
        }
    }
}
