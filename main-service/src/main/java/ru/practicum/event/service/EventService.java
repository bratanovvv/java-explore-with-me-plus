package ru.practicum.event.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.model.Category;
import ru.practicum.category.service.CategoryService;
import ru.practicum.event.dto.query.AdminEventFilter;
import ru.practicum.event.dto.query.EventSort;
import ru.practicum.event.dto.query.PublicEventFilter;
import ru.practicum.event.dto.request.EventStateAction;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventState;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.user.model.User;
import ru.practicum.user.service.UserService;
import ru.practicum.utils.error.exception.NotFoundException;
import ru.practicum.utils.params.PageParams;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@Transactional(readOnly = true)
public class EventService {

    private static final Sort ADMIN_SORT =
            Sort.by(Sort.Direction.ASC, "id");
    private static final Sort PUBLIC_DATE_SORT =
            Sort.by(Sort.Direction.ASC, "eventDate").and(Sort.by(Sort.Direction.ASC, "id"));

    private final EventRepository eventRepository;
    private final UserService userService;
    private final CategoryService categoryService;
    private final EventValidator eventValidator;
    private final EventStateMachine stateMachine;

    public EventService(EventRepository eventRepository,
                        EventValidator eventValidator,
                        EventStateMachine stateMachine,
                        UserService userService,
                        CategoryService categoryService) {
        this.eventRepository = eventRepository;
        this.userService = userService;
        this.eventValidator = eventValidator;
        this.stateMachine = stateMachine;
        this.categoryService = categoryService;
    }

    public List<Event> getUserEvents(Long userId, PageParams pageParams) {
        return eventRepository.findAllByInitiatorId(userId, pageParams.toPageable(ADMIN_SORT)).getContent();
    }

    public Event getUserEvent(Long userId, Long eventId) {
        return eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));
    }

    @Transactional
    public Event createEvent(Long userId, Event event) {
        User initiator = userService.getUser(userId);
        Category category = categoryService.getCategory(event.getCategory().getId());
        eventValidator.validateEventDate(event.getEventDate());

        applyDefaults(event);

        event.setInitiator(initiator);
        event.setCategory(category);
        event.setState(EventState.PENDING);
        event.setCreatedOn(LocalDateTime.now());
        Event savedEvent = eventRepository.save(event);
        log.debug("Создано событие id={}", savedEvent.getId());
        return savedEvent;
    }

    @Transactional
    public Event updateEventByUser(Long userId, Long eventId, Event updates, EventStateAction stateAction) {
        Event event = getUserEvent(userId, eventId);
        stateMachine.validateUserCanUpdate(event);
        if (stateAction != null) {
            stateMachine.applyUserAction(event, stateAction);
        }
        applyUpdates(event, updates);
        log.debug("Обновлено событие id={} автором userId={}", eventId, userId);
        return event;
    }

    @Transactional
    public Event updateEventByAdmin(Long eventId, Event updates, EventStateAction stateAction) {
        Event event = getEvent(eventId);
        applyUpdates(event, updates);
        if (stateAction == EventStateAction.PUBLISH_EVENT) {
            eventValidator.validatePublicationDate(event, LocalDateTime.now());
        }
        if (stateAction != null) {
            stateMachine.applyAdminAction(event, stateAction);
        }
        log.debug("Обновлено событие id={} администратором", eventId);
        return event;
    }

    public List<Event> getPublicEvents(PublicEventFilter filter, PageParams pageParams) {
        // TODO: пробросить filter.onlyAvailable() в репозиторий (см. findPublicEvents).
        eventValidator.validateDateRange(filter.rangeStart(), filter.rangeEnd());
        LocalDateTime start = filter.rangeStart();
        if (start == null && filter.rangeEnd() == null) {
            start = LocalDateTime.now();
        }

        Pageable pageable = filter.sort() == EventSort.VIEWS
                ? Pageable.unpaged()
                : pageParams.toPageable(PUBLIC_DATE_SORT);
        return eventRepository.findPublicEvents(
                EventState.PUBLISHED, normalizeText(filter.text()), filter.categories(), filter.paid(),
                start, filter.rangeEnd(), pageable);
    }

    public List<Event> getAdminEvents(AdminEventFilter filter, PageParams pageParams) {
        eventValidator.validateDateRange(filter.rangeStart(), filter.rangeEnd());
        return eventRepository.findAdminEvents(
                filter.users(), filter.states(), filter.categories(),
                filter.rangeStart(), filter.rangeEnd(),
                pageParams.toPageable(ADMIN_SORT));
    }

    public Event getPublicEvent(Long eventId) {
        return getEvent(eventId, EventState.PUBLISHED);
    }

    private Event getEvent(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));
    }

    private Event getEvent(Long eventId, EventState state) {
        return eventRepository.findByIdAndState(eventId, state)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));
    }

    private void applyDefaults(Event event) {
        event.setPaid(Objects.requireNonNullElse(event.getPaid(), false));
        event.setParticipantLimit(Objects.requireNonNullElse(event.getParticipantLimit(), 0));
        event.setRequestModeration(Objects.requireNonNullElse(event.getRequestModeration(), true));
    }

    private void applyUpdates(Event event, Event updates) {
        if (updates.getAnnotation() != null) {
            event.setAnnotation(updates.getAnnotation());
        }
        if (updates.getCategory() != null) {
            event.setCategory(categoryService.getCategory(updates.getCategory().getId()));
        }
        if (updates.getDescription() != null) {
            event.setDescription(updates.getDescription());
        }
        if (updates.getEventDate() != null) {
            eventValidator.validateEventDate(updates.getEventDate());
            event.setEventDate(updates.getEventDate());
        }
        if (updates.getLocation() != null) {
            event.setLocation(updates.getLocation());
        }
        if (updates.getPaid() != null) {
            event.setPaid(updates.getPaid());
        }
        if (updates.getParticipantLimit() != null) {
            event.setParticipantLimit(updates.getParticipantLimit());
        }
        if (updates.getRequestModeration() != null) {
            event.setRequestModeration(updates.getRequestModeration());
        }
        if (updates.getTitle() != null) {
            event.setTitle(updates.getTitle());
        }
    }

    private static String normalizeText(String text) {
        return text == null || text.isBlank() ? null : text.trim();
    }
}
