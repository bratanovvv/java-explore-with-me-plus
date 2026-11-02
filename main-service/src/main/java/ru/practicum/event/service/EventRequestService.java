package ru.practicum.event.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.event.model.Event;
import ru.practicum.request.dto.RequestStatusUpdate;
import ru.practicum.request.dto.RequestStatusUpdateResult;
import ru.practicum.request.mapper.RequestMapper;
import ru.practicum.request.model.Request;
import ru.practicum.request.model.RequestStatus;
import ru.practicum.request.service.RequestService;

import java.util.ArrayList;
import java.util.List;

/**
 * Модерация заявок на участие владельцем события.
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class EventRequestService {

    private final EventService eventService;
    private final RequestService requestService;
    private final EventValidator eventValidator;
    private final RequestMapper requestMapper;

    public EventRequestService(EventService eventService,
                               RequestService requestService,
                               EventValidator eventValidator,
                               RequestMapper requestMapper) {
        this.eventService = eventService;
        this.requestService = requestService;
        this.eventValidator = eventValidator;
        this.requestMapper = requestMapper;
    }

    public List<Request> getEventRequests(Long userId, Long eventId) {
        log.debug("Получение заявок события eventId={} владельцем userId={}", eventId, userId);
        eventService.getUserEvent(userId, eventId);
        return requestService.getEventRequests(eventId);
    }

    @Transactional
    public RequestStatusUpdateResult changeRequestStatus(Long userId, Long eventId, RequestStatusUpdate update) {
        log.debug("Изменение статуса заявок события eventId={} владельцем userId={} на {}",
                eventId, userId, update.getStatus());
        eventValidator.validateRequestCanChangeStatus(update.getStatus());

        Event event = eventService.getEventWithLock(eventId);
        eventValidator.validateEventOwner(event, userId);
        eventValidator.validateRequestModeration(event);

        List<Request> requests = requestService.getRequestsByIds(update.getRequestIds());
        eventValidator.validateEventRequests(eventId, update.getRequestIds(), requests);
        eventValidator.validateRequestsArePending(requests);

        return update.getStatus() == RequestStatus.CONFIRMED
                ? confirmRequests(event, requests)
                : rejectRequests(requests);
    }

    private RequestStatusUpdateResult confirmRequests(Event event, List<Request> requests) {
        int limit = event.getParticipantLimit();
        long confirmedCount = requestService.countConfirmedRequests(event.getId());
        eventValidator.validateParticipantLimit(limit, confirmedCount);

        List<Request> confirmed = new ArrayList<>();
        List<Request> rejected = new ArrayList<>();
        for (Request request : requests) {
            if (confirmedCount + confirmed.size() < limit) {
                request.setStatus(RequestStatus.CONFIRMED);
                confirmed.add(request);
            } else {
                request.setStatus(RequestStatus.REJECTED);
                rejected.add(request);
            }
        }

        if (confirmedCount + confirmed.size() == limit) {
            List<Request> pending = requestService.getEventRequestsByStatus(
                    List.of(event.getId()), RequestStatus.PENDING);
            pending.forEach(request -> request.setStatus(RequestStatus.REJECTED));
            rejected.addAll(pending);
        }
        return buildResult(confirmed, rejected);
    }

    private RequestStatusUpdateResult rejectRequests(List<Request> requests) {
        requests.forEach(request -> request.setStatus(RequestStatus.REJECTED));
        return buildResult(List.of(), requests);
    }

    private RequestStatusUpdateResult buildResult(List<Request> confirmed, List<Request> rejected) {
        return new RequestStatusUpdateResult(
                requestMapper.toDtoList(confirmed),
                requestMapper.toDtoList(rejected));
    }
}
