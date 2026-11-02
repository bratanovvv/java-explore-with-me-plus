package ru.practicum.event.service;

import org.springframework.stereotype.Component;
import ru.practicum.event.model.Event;
import ru.practicum.request.model.Request;
import ru.practicum.request.model.RequestStatus;
import ru.practicum.utils.error.exception.BadRequestException;
import ru.practicum.utils.error.exception.ConflictException;
import ru.practicum.utils.error.exception.NotFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

    /**
     * Модерировать заявки можно только переводом в подтверждение или отклонение.
     */
    public void validateRequestCanChangeStatus(RequestStatus status) {
        if (status != RequestStatus.CONFIRMED && status != RequestStatus.REJECTED) {
            throw new BadRequestException("Request status must be CONFIRMED or REJECTED");
        }
    }

    /**
     * Модерировать заявки может только владелец события.
     */
    public void validateEventOwner(Event event, Long userId) {
        if (!event.getInitiator().getId().equals(userId)) {
            throw new NotFoundException("Event with id=" + event.getId() + " was not found");
        }
    }

    /**
     * Подтверждение заявок требуется только при включённой модерации и ненулевом лимите.
     */
    public void validateRequestModeration(Event event) {
        if (event.getParticipantLimit() == 0 || !event.getRequestModeration()) {
            throw new ConflictException("Event with id=" + event.getId() + " does not require requests confirmation");
        }
    }

    /**
     * Все указанные заявки должны существовать и относиться к данному событию.
     */
    public void validateEventRequests(Long eventId, List<Long> requestIds, List<Request> requests) {
        Set<Long> foundIds = requests.stream()
                .map(Request::getId)
                .collect(Collectors.toSet());
        for (Long requestId : requestIds) {
            if (!foundIds.contains(requestId)) {
                throw new NotFoundException("Request with id=" + requestId + " was not found");
            }
        }
        for (Request request : requests) {
            if (!request.getEvent().getId().equals(eventId)) {
                throw new NotFoundException("Request with id=" + request.getId() + " was not found");
            }
        }
    }

    /**
     * Менять статус можно только у заявок в ожидании.
     */
    public void validateRequestsArePending(List<Request> requests) {
        for (Request request : requests) {
            if (request.getStatus() != RequestStatus.PENDING) {
                throw new ConflictException("Request must have status PENDING");
            }
        }
    }

    /**
     * Подтверждать заявки, когда лимит уже достигнут, нельзя.
     */
    public void validateParticipantLimit(int participantLimit, long confirmedRequests) {
        if (confirmedRequests >= participantLimit) {
            throw new ConflictException("The participant limit has been reached");
        }
    }
}
