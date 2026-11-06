package ru.practicum.event.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.event.model.Event;
import ru.practicum.request.model.Request;
import ru.practicum.request.model.RequestStatus;
import ru.practicum.user.model.User;
import ru.practicum.utils.error.exception.BadRequestException;
import ru.practicum.utils.error.exception.ConflictException;
import ru.practicum.utils.error.exception.NotFoundException;

import java.time.LocalDateTime;
import java.util.List;

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

    @Test
    void validateRequestStatus_shouldAcceptConfirmedAndRejected() {
        eventValidator.validateRequestCanChangeStatus(RequestStatus.CONFIRMED);
        eventValidator.validateRequestCanChangeStatus(RequestStatus.REJECTED);
    }

    @Test
    void validateRequestStatus_shouldRejectOtherStatuses() {
        assertThatThrownBy(() -> eventValidator.validateRequestCanChangeStatus(RequestStatus.PENDING))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Request status must be CONFIRMED or REJECTED");
    }

    @Test
    void validateEventOwner_shouldAcceptOwner() {
        Event event = buildOwnedEvent(5L);

        eventValidator.validateEventOwner(event, 5L);
    }

    @Test
    void validateEventOwner_shouldRejectAnotherUser() {
        Event event = buildOwnedEvent(5L);

        assertThatThrownBy(() -> eventValidator.validateEventOwner(event, 6L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Event with id=1 was not found");
    }

    @Test
    void validateRequestModeration_shouldAcceptModeratedEventWithLimit() {
        eventValidator.validateRequestModeration(Event.builder()
                .id(1L)
                .participantLimit(10)
                .requestModeration(true)
                .build());
    }

    @Test
    void validateRequestModeration_shouldRejectEventWithoutLimit() {
        Event event = Event.builder()
                .id(1L)
                .participantLimit(0)
                .requestModeration(true)
                .build();

        assertThatThrownBy(() -> eventValidator.validateRequestModeration(event))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("does not require requests confirmation");
    }

    @Test
    void validateRequestModeration_shouldRejectEventWithoutModeration() {
        Event event = Event.builder()
                .id(1L)
                .participantLimit(10)
                .requestModeration(false)
                .build();

        assertThatThrownBy(() -> eventValidator.validateRequestModeration(event))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("does not require requests confirmation");
    }

    @Test
    void validateEventRequests_shouldAcceptRequestsOfEvent() {
        Request request = buildRequest(1L, 10L);

        eventValidator.validateEventRequests(10L, List.of(1L), List.of(request));
    }

    @Test
    void validateEventRequests_whenRequestMissing_shouldThrowNotFoundException() {
        assertThatThrownBy(() -> eventValidator.validateEventRequests(10L, List.of(1L), List.of()))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Request with id=1 was not found");
    }

    @Test
    void validateEventRequests_whenRequestBelongsToAnotherEvent_shouldThrowNotFoundException() {
        Request request = buildRequest(1L, 99L);

        assertThatThrownBy(() -> eventValidator.validateEventRequests(10L, List.of(1L), List.of(request)))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Request with id=1 was not found");
    }

    @Test
    void validateRequestsArePending_shouldAcceptPendingRequests() {
        eventValidator.validateRequestsArePending(List.of(buildRequest(1L, 10L, RequestStatus.PENDING)));
    }

    @Test
    void validateRequestsArePending_whenStatusIsNotPending_shouldThrowConflictException() {
        List<Request> requests = List.of(buildRequest(1L, 10L, RequestStatus.CONFIRMED));

        assertThatThrownBy(() -> eventValidator.validateRequestsArePending(requests))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Request must have status PENDING");
    }

    @Test
    void validateParticipantLimit_shouldAcceptFreePlace() {
        eventValidator.validateParticipantLimit(5, 4L);
    }

    @Test
    void validateParticipantLimit_whenLimitReached_shouldThrowConflictException() {
        assertThatThrownBy(() -> eventValidator.validateParticipantLimit(5, 5L))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("The participant limit has been reached");
    }

    private Event buildOwnedEvent(Long initiatorId) {
        return Event.builder()
                .id(1L)
                .initiator(User.builder().id(initiatorId).build())
                .build();
    }

    private Request buildRequest(Long id, Long eventId) {
        return Request.builder()
                .id(id)
                .event(Event.builder().id(eventId).build())
                .status(RequestStatus.PENDING)
                .build();
    }

    private Request buildRequest(Long id, Long eventId, RequestStatus status) {
        return Request.builder()
                .id(id)
                .event(Event.builder().id(eventId).build())
                .status(status)
                .build();
    }
}
