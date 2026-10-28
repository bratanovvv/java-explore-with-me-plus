package ru.practicum.event.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
import ru.practicum.utils.error.exception.ConflictException;
import ru.practicum.utils.error.exception.NotFoundException;
import ru.practicum.utils.params.PageParams;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private UserService userService;

    @Mock
    private EventValidator eventValidator;

    @Mock
    private EventStateMachine stateMachine;

    @Mock
    private CategoryService categoryService;

    private EventService eventService;

    @BeforeEach
    void setUp() {
        eventService = new EventService(eventRepository, eventValidator, stateMachine, userService, categoryService);
    }

    @Test
    void createEvent_shouldSavePendingEventWithDefaults() {
        User initiator = User.builder().id(1L).name("Иван").email("ivan@mail.ru").build();
        Category category = Category.builder().id(2L).name("Концерты").build();
        Event event = buildNewEvent();
        when(userService.getUser(1L)).thenReturn(initiator);
        when(categoryService.getCategory(2L)).thenReturn(category);
        when(eventRepository.save(event)).thenReturn(event);

        Event savedEvent = eventService.createEvent(1L, event);

        assertThat(savedEvent).isSameAs(event);
        assertThat(event.getInitiator()).isEqualTo(initiator);
        assertThat(event.getCategory()).isEqualTo(category);
        assertThat(event.getState()).isEqualTo(EventState.PENDING);
        assertThat(event.getCreatedOn()).isNotNull();
        assertThat(event.getPaid()).isFalse();
        assertThat(event.getParticipantLimit()).isZero();
        assertThat(event.getRequestModeration()).isTrue();
        verify(eventValidator).validateEventDate(event.getEventDate());
    }

    @Test
    void createEvent_shouldThrowWhenUserMissing() {
        Event event = buildNewEvent();
        when(userService.getUser(1L)).thenThrow(new NotFoundException("User with id=1 was not found"));

        assertThatThrownBy(() -> eventService.createEvent(1L, event))
                .isInstanceOf(NotFoundException.class);
        verifyNoInteractions(eventValidator);
        verify(eventRepository, never()).save(any());
    }

    @Test
    void getUserEvent_shouldThrowWhenEventIsNotUsers() {
        when(eventRepository.findByIdAndInitiatorId(5L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventService.getUserEvent(1L, 5L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getUserEvent_shouldReturnEntity() {
        Event event = Event.builder().id(5L).build();
        when(eventRepository.findByIdAndInitiatorId(5L, 1L)).thenReturn(Optional.of(event));

        assertThat(eventService.getUserEvent(1L, 5L)).isSameAs(event);
    }

    @Test
    void updateEventByUser_shouldRejectPublishedEvent() {
        Event event = Event.builder().id(5L).state(EventState.PUBLISHED).build();
        when(eventRepository.findByIdAndInitiatorId(5L, 1L)).thenReturn(Optional.of(event));
        doThrow(new ConflictException("Only pending or canceled events can be changed"))
                .when(stateMachine).validateUserCanUpdate(event);

        assertThatThrownBy(() -> eventService.updateEventByUser(
                1L, 5L, Event.builder().build(), EventStateAction.CANCEL_REVIEW))
                .isInstanceOf(ConflictException.class);
        verify(stateMachine).validateUserCanUpdate(event);
        verify(stateMachine, never()).applyUserAction(any(), any());
    }

    @Test
    void updateEventByUser_shouldApplyFieldsAndStateAction() {
        Event event = Event.builder().id(5L).state(EventState.PENDING).title("Старое").build();
        Event updates = Event.builder().title("Новое название").build();
        when(eventRepository.findByIdAndInitiatorId(5L, 1L)).thenReturn(Optional.of(event));

        Event updated = eventService.updateEventByUser(1L, 5L, updates, EventStateAction.CANCEL_REVIEW);

        assertThat(updated).isSameAs(event);
        assertThat(event.getTitle()).isEqualTo("Новое название");
        verify(stateMachine).applyUserAction(event, EventStateAction.CANCEL_REVIEW);
        verify(eventValidator, never()).validateEventDate(any());
    }

    @Test
    void updateEventByUser_shouldResolveNewCategory() {
        Event event = Event.builder().id(5L).state(EventState.CANCELED).build();
        Event updates = Event.builder().category(Category.builder().id(9L).build()).build();
        Category category = Category.builder().id(9L).name("Новая").build();
        when(eventRepository.findByIdAndInitiatorId(5L, 1L)).thenReturn(Optional.of(event));
        when(categoryService.getCategory(9L)).thenReturn(category);

        eventService.updateEventByUser(1L, 5L, updates, null);

        assertThat(event.getCategory()).isEqualTo(category);
        verify(stateMachine, never()).applyUserAction(any(), any());
    }

    @Test
    void updateEventByAdmin_shouldPublishEvent() {
        Event event = Event.builder()
                .id(7L)
                .state(EventState.PENDING)
                .eventDate(LocalDateTime.now().plusDays(1))
                .build();
        when(eventRepository.findById(7L)).thenReturn(Optional.of(event));

        Event updated = eventService.updateEventByAdmin(
                7L, Event.builder().build(), EventStateAction.PUBLISH_EVENT);

        assertThat(updated).isSameAs(event);
        verify(eventValidator).validatePublicationDate(eq(event), any(LocalDateTime.class));
        verify(stateMachine).applyAdminAction(event, EventStateAction.PUBLISH_EVENT);
    }

    @Test
    void updateEventByAdmin_shouldThrowWhenEventMissing() {
        when(eventRepository.findById(8L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventService.updateEventByAdmin(
                8L, Event.builder().build(), EventStateAction.REJECT_EVENT))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getAdminEvents_shouldUseOffsetPagination() {
        Event event = Event.builder().id(1L).build();
        when(eventRepository.findAdminEvents(
                any(), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(List.of(event));

        List<Event> events = eventService.getAdminEvents(
                new AdminEventFilter(
                        List.of(1L), List.of(EventState.PUBLISHED), List.of(2L), null, null),
                new PageParams(5, 20));

        assertThat(events).containsExactly(event);
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(eventRepository).findAdminEvents(
                any(), any(), any(), any(), any(), pageableCaptor.capture());
        assertThat(pageableCaptor.getValue().getOffset()).isEqualTo(5);
        assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(20);
        verify(eventValidator).validateDateRange(null, null);
    }

    @Test
    void getPublicEvents_shouldUseDatabasePaginationForEventDateSort() {
        Event event = Event.builder().id(1L).build();
        when(eventRepository.findPublicEvents(
                any(), any(), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(List.of(event));

        List<Event> events = eventService.getPublicEvents(
                new PublicEventFilter("text", List.of(2L), true, null, null, false, EventSort.EVENT_DATE),
                new PageParams(0, 10));

        assertThat(events).containsExactly(event);
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(eventRepository).findPublicEvents(
                any(), any(), any(), any(), any(), any(), pageableCaptor.capture());
        assertThat(pageableCaptor.getValue().getSort())
                .isEqualTo(Sort.by(Sort.Direction.ASC, "eventDate").and(Sort.by(Sort.Direction.ASC, "id")));
    }

    @Test
    void getPublicEvents_shouldDefaultToEventDateSort() {
        when(eventRepository.findPublicEvents(
                any(), any(), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(List.of());

        List<Event> events = eventService.getPublicEvents(
                new PublicEventFilter(null, null, null, null, null, false, null),
                new PageParams(0, 10));

        assertThat(events).isEmpty();
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(eventRepository).findPublicEvents(
                any(), any(), any(), any(), any(), any(), pageableCaptor.capture());
        assertThat(pageableCaptor.getValue().isPaged()).isTrue();
    }

    @Test
    void getPublicEvents_shouldReturnAllMatchesForViewsSort() {
        Event first = Event.builder().id(1L).build();
        Event second = Event.builder().id(2L).build();
        when(eventRepository.findPublicEvents(
                any(), any(), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(List.of(first, second));

        List<Event> events = eventService.getPublicEvents(
                new PublicEventFilter(null, null, null, null, null, false, EventSort.VIEWS),
                new PageParams(1, 1));

        assertThat(events).containsExactly(first, second);
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(eventRepository).findPublicEvents(
                any(), any(), any(), any(), any(), any(), pageableCaptor.capture());
        assertThat(pageableCaptor.getValue().isUnpaged()).isTrue();
    }

    @Test
    void getPublicEvent_shouldRejectUnpublishedEvent() {
        when(eventRepository.findByIdAndState(9L, EventState.PUBLISHED)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventService.getPublicEvent(9L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getPublicEvent_shouldReturnPublishedEvent() {
        Event event = Event.builder().id(9L).state(EventState.PUBLISHED).build();
        when(eventRepository.findByIdAndState(9L, EventState.PUBLISHED)).thenReturn(Optional.of(event));

        assertThat(eventService.getPublicEvent(9L)).isSameAs(event);
    }

    @Test
    void getUserEvents_shouldReturnUsersEvents() {
        Event event = Event.builder().id(1L).build();
        when(eventRepository.findAllByInitiatorId(eq(1L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(event)));

        List<Event> events = eventService.getUserEvents(1L, new PageParams(0, 10));

        assertThat(events).containsExactly(event);
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(eventRepository).findAllByInitiatorId(eq(1L), pageableCaptor.capture());
        assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(10);
    }

    private Event buildNewEvent() {
        return Event.builder()
                .annotation("Аннотация нового события")
                .category(Category.builder().id(2L).build())
                .description("Описание нового события")
                .eventDate(LocalDateTime.now().plusDays(1))
                .title("Событие")
                .build();
    }
}
