package ru.practicum.event.service;

import org.springframework.stereotype.Component;
import ru.practicum.event.dto.request.EventStateAction;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventState;
import ru.practicum.utils.error.exception.BadRequestException;
import ru.practicum.utils.error.exception.ConflictException;

import java.time.LocalDateTime;

@Component
public class EventStateMachine {

    public void applyUserAction(Event event, EventStateAction action) {
        switch (action) {
            case SEND_TO_REVIEW -> transition(event, EventState.CANCELED, EventState.PENDING, action);
            case CANCEL_REVIEW -> transition(event, EventState.PENDING, EventState.CANCELED, action);
            default -> throw new BadRequestException("State action " + action + " is not allowed for the event author");
        }
    }

    public void applyAdminAction(Event event, EventStateAction action) {
        switch (action) {
            case PUBLISH_EVENT -> {
                transition(event, EventState.PENDING, EventState.PUBLISHED, action);
                event.setPublishedOn(LocalDateTime.now());
            }
            case REJECT_EVENT -> transition(event, EventState.PENDING, EventState.CANCELED, action);
            default -> throw new BadRequestException("State action " + action + " is not allowed for the administrator");
        }
    }

    /**
     * Автор может изменять только событие, которое ещё не опубликовано.
     */
    public void validateUserCanUpdate(Event event) {
        if (event.getState() != EventState.PENDING && event.getState() != EventState.CANCELED) {
            throw new ConflictException("Only pending or canceled events can be changed");
        }
    }

    private void transition(Event event, EventState from, EventState to, EventStateAction action) {
        requireState(event, from, action);
        event.setState(to);
    }

    private void requireState(Event event, EventState expected, EventStateAction action) {
        if (event.getState() != expected) {
            throw new ConflictException("Cannot apply " + action + " to the event with id=" + event.getId()
                    + " because it's not in the right state: " + event.getState());
        }
    }
}
