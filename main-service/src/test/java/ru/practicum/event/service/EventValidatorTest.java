package ru.practicum.event.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.event.model.Event;
import ru.practicum.utils.error.exception.BadRequestException;
import ru.practicum.utils.error.exception.ConflictException;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EventValidatorTest {

    private EventValidator eventValidator;

    @BeforeEach
    void setUp() {
        eventValidator = new EventValidator();
    }

    @Test
    void validateEventDate_shouldAcceptDateFarEnoughInFuture() {
        eventValidator.validateEventDate(LocalDateTime.now().plusHours(3));
    }

    @Test
    void validateEventDate_shouldRejectDateTooSoon() {
        LocalDateTime eventDate = LocalDateTime.now().plusHours(1);

        assertThatThrownBy(() -> eventValidator.validateEventDate(eventDate))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void validatePublicationDate_shouldAcceptDateFarEnoughInFuture() {
        Event event = Event.builder().id(1L).eventDate(LocalDateTime.now().plusHours(3)).build();

        eventValidator.validatePublicationDate(event, LocalDateTime.now());
    }

    @Test
    void validatePublicationDate_shouldRejectDateTooSoon() {
        Event event = Event.builder().id(1L).eventDate(LocalDateTime.now().plusMinutes(30)).build();

        assertThatThrownBy(() -> eventValidator.validatePublicationDate(event, LocalDateTime.now()))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("id=1");
    }

    @Test
    void validateDateRange_shouldRejectInvertedRange() {
        LocalDateTime start = LocalDateTime.now().plusDays(2);
        LocalDateTime end = LocalDateTime.now().plusDays(1);

        assertThatThrownBy(() -> eventValidator.validateDateRange(start, end))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void validateDateRange_shouldAcceptNullBounds() {
        eventValidator.validateDateRange(null, LocalDateTime.now());
        eventValidator.validateDateRange(LocalDateTime.now(), null);
    }
}
