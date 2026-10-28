package ru.practicum.event.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.event.dto.request.EventStateAction;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventState;
import ru.practicum.utils.error.exception.BadRequestException;
import ru.practicum.utils.error.exception.ConflictException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EventStateMachineTest {

    private EventStateMachine stateMachine;

    @BeforeEach
    void setUp() {
        stateMachine = new EventStateMachine();
    }

    @Test
    void publish_shouldPublishPendingEvent() {
        Event event = Event.builder().id(1L).state(EventState.PENDING).build();

        stateMachine.applyAdminAction(event, EventStateAction.PUBLISH_EVENT);

        assertThat(event.getState()).isEqualTo(EventState.PUBLISHED);
        assertThat(event.getPublishedOn()).isNotNull();
    }

    @Test
    void publish_shouldRejectAlreadyPublishedEvent() {
        Event event = Event.builder().id(1L).state(EventState.PUBLISHED).build();

        assertThatThrownBy(() -> stateMachine.applyAdminAction(event, EventStateAction.PUBLISH_EVENT))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void reject_shouldCancelPendingEvent() {
        Event event = Event.builder().id(1L).state(EventState.PENDING).build();

        stateMachine.applyAdminAction(event, EventStateAction.REJECT_EVENT);

        assertThat(event.getState()).isEqualTo(EventState.CANCELED);
    }

    @Test
    void reject_shouldRejectCanceledEvent() {
        Event event = Event.builder().id(1L).state(EventState.CANCELED).build();

        assertThatThrownBy(() -> stateMachine.applyAdminAction(event, EventStateAction.REJECT_EVENT))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void sendToReview_shouldReturnCanceledEventToPending() {
        Event event = Event.builder().id(1L).state(EventState.CANCELED).build();

        stateMachine.applyUserAction(event, EventStateAction.SEND_TO_REVIEW);

        assertThat(event.getState()).isEqualTo(EventState.PENDING);
    }

    @Test
    void sendToReview_shouldRejectPendingEvent() {
        Event event = Event.builder().id(1L).state(EventState.PENDING).build();

        assertThatThrownBy(() -> stateMachine.applyUserAction(event, EventStateAction.SEND_TO_REVIEW))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void cancelReview_shouldCancelPendingEvent() {
        Event event = Event.builder().id(1L).state(EventState.PENDING).build();

        stateMachine.applyUserAction(event, EventStateAction.CANCEL_REVIEW);

        assertThat(event.getState()).isEqualTo(EventState.CANCELED);
    }

    @Test
    void cancelReview_shouldRejectCanceledEvent() {
        Event event = Event.builder().id(1L).state(EventState.CANCELED).build();

        assertThatThrownBy(() -> stateMachine.applyUserAction(event, EventStateAction.CANCEL_REVIEW))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void validateUserCanUpdate_shouldAcceptPendingAndCanceledEvents() {
        stateMachine.validateUserCanUpdate(Event.builder().state(EventState.PENDING).build());
        stateMachine.validateUserCanUpdate(Event.builder().state(EventState.CANCELED).build());
    }

    @Test
    void validateUserCanUpdate_shouldRejectPublishedEvent() {
        Event event = Event.builder().state(EventState.PUBLISHED).build();

        assertThatThrownBy(() -> stateMachine.validateUserCanUpdate(event))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Only pending or canceled events can be changed");
    }

    @Test
    void authorAction_shouldRejectAdminStateAction() {
        Event event = Event.builder().id(1L).state(EventState.PENDING).build();

        assertThatThrownBy(() -> stateMachine.applyUserAction(event, EventStateAction.PUBLISH_EVENT))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void adminAction_shouldRejectAuthorStateAction() {
        Event event = Event.builder().id(1L).state(EventState.PENDING).build();

        assertThatThrownBy(() -> stateMachine.applyAdminAction(event, EventStateAction.CANCEL_REVIEW))
                .isInstanceOf(BadRequestException.class);
    }
}
