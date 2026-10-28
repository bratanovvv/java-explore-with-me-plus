package ru.practicum.event.service;

import org.springframework.stereotype.Component;
import ru.practicum.event.model.Event;
import ru.practicum.utils.error.exception.BadRequestException;
import ru.practicum.utils.error.exception.ConflictException;

import java.time.LocalDateTime;

@Component
public class EventValidator {

    /**
     * Минимальный запас времени до даты события при создании и изменении.
     */
    private static final long HOURS_BEFORE_EVENT = 2;

    /**
     * Минимальный запас времени до даты события при публикации.
     */
    private static final long HOURS_BEFORE_PUBLICATION = 1;

    /**
     * Дата события должна быть не раньше чем через два часа от текущего момента.
     */
    public void validateEventDate(LocalDateTime eventDate) {
        if (eventDate.isBefore(LocalDateTime.now().plusHours(HOURS_BEFORE_EVENT))) {
            throw new BadRequestException("Field: eventDate. Error: должно содержать дату, которая еще не наступила. "
                    + "Value: " + eventDate);
        }
    }

    /**
     * Публиковать можно только событие, до которого осталось не меньше часа.
     */
    public void validatePublicationDate(Event event, LocalDateTime time) {
        if (event.getEventDate().isBefore(time.plusHours(HOURS_BEFORE_PUBLICATION))) {
            throw new ConflictException("Event with id=" + event.getId()
                    + " cannot be published less than an hour before the event date");
        }
    }

    public void validateDateRange(LocalDateTime rangeStart, LocalDateTime rangeEnd) {
        if (rangeStart != null && rangeEnd != null && rangeStart.isAfter(rangeEnd)) {
            throw new BadRequestException("rangeStart must be before or equal to rangeEnd");
        }
    }
}
