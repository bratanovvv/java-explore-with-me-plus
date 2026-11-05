package ru.practicum.request.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventState;
import ru.practicum.event.service.EventService;
import ru.practicum.request.model.Request;
import ru.practicum.request.model.RequestStatus;
import ru.practicum.request.repository.RequestRepository;
import ru.practicum.user.model.User;
import ru.practicum.user.service.UserService;
import ru.practicum.utils.error.exception.ConflictException;
import ru.practicum.utils.error.exception.NotFoundException;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
public class RequestService {

    private final RequestRepository requestRepository;
    private final EventService eventService;
    private final UserService userService;

    public RequestService(RequestRepository requestRepository,
                          EventService eventService,
                          UserService userService) {
        this.requestRepository = requestRepository;
        this.eventService = eventService;
        this.userService = userService;
    }

    public List<Request> getUserRequests(Long userId) {
        log.debug("Получение всех заявок пользователя userId={}", userId);
        userService.getUser(userId);
        return requestRepository.findAllByRequesterId(userId);
    }

    public List<Request> getEventRequests(Long eventId) {
        return requestRepository.findAllByEventId(eventId);
    }

    public List<Request> getRequestsByIds(Collection<Long> requestIds) {
        return requestRepository.findAllByIdIn(requestIds);
    }

    public List<Request> getEventRequestsByStatus(Collection<Long> eventIds, RequestStatus status) {
        return requestRepository.findAllByEventIdInAndStatus(eventIds, status);
    }

    public long countConfirmedRequests(Long eventId) {
        return requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
    }

    @Transactional
    public void updateStatuses(Collection<Long> requestIds, RequestStatus status, Long eventId) {
        if (requestIds.isEmpty()) {
            return;
        }
        requestRepository.updateStatusByIdsInEvent(requestIds, status, eventId);
    }

    @Transactional
    public void rejectPending(Long eventId) {
        requestRepository.updateStatusByEventAndStatus(eventId, RequestStatus.PENDING, RequestStatus.REJECTED);
    }

    @Transactional
    public Request createRequest(Long userId, Long eventId) {
        log.debug("Создание заявки пользователем userId={} на событие eventId={}", userId, eventId);
        User requester = userService.getUser(userId);
        Event event = eventService.getEventWithLock(eventId);

        validateCreateRequest(userId, event);

        boolean autoConfirm = event.getParticipantLimit() == 0 || !event.getRequestModeration();
        RequestStatus status = autoConfirm
                ? RequestStatus.CONFIRMED
                : RequestStatus.PENDING;

        Request request = Request.builder()
                .event(event)
                .requester(requester)
                .created(LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS))
                .status(status)
                .build();

        return requestRepository.save(request);
    }

    @Transactional
    public Request cancelRequest(Long userId, Long requestId) {
        log.debug("Отмена заявки requestId={} пользователем userId={}", requestId, userId);
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Request with id=" + requestId + " was not found"));

        validateCancelRequest(userId, request);

        request.setStatus(RequestStatus.CANCELED);
        return request;
    }

    private void validateCreateRequest(Long userId, Event event) {
        if (event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Initiator cannot add a request to participate in his event");
        }

        if (event.getState() != EventState.PUBLISHED) {
            throw new ConflictException("Cannot participate in an unpublished event");
        }

        if (requestRepository.existsByEventIdAndRequesterId(event.getId(), userId)) {
            throw new ConflictException("Request already exists");
        }

        if (event.getParticipantLimit() > 0) {
            long confirmedCount = requestRepository.countByEventIdAndStatus(event.getId(), RequestStatus.CONFIRMED);
            if (confirmedCount >= event.getParticipantLimit()) {
                throw new ConflictException("Participant limit has been reached");
            }
        }
    }

    private void validateCancelRequest(Long userId, Request request) {
        if (!request.getRequester().getId().equals(userId)) {
            throw new NotFoundException("Request with id=" + request.getId() + " was not found for user " + userId);
        }
        if (request.getStatus() != RequestStatus.PENDING) {
            throw new ConflictException("Only pending requests can be canceled");
        }
    }
}
