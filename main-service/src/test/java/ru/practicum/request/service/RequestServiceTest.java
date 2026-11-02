package ru.practicum.request.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RequestServiceTest {

    @Mock
    private RequestRepository requestRepository;
    @Mock
    private EventService eventService;
    @Mock
    private UserService userService;
    @InjectMocks
    private RequestService requestService;

    @Test
    void getUserRequests_whenUserExists_shouldReturnList() {
        Long userId = 1L;
        Request request = new Request();
        when(userService.getUser(userId)).thenReturn(new User());
        when(requestRepository.findAllByRequesterId(userId)).thenReturn(List.of(request));

        List<Request> result = requestService.getUserRequests(userId);

        assertThat(result).containsExactly(request);
    }

    @Test
    void getUserRequests_whenUserNotFound_shouldThrowNotFoundException() {
        when(userService.getUser(99L))
                .thenThrow(new NotFoundException("User with id=99 was not found"));

        assertThatThrownBy(() -> requestService.getUserRequests(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User with id=99 was not found");
    }

    @Test
    void createRequest_whenValidAndModerationTrue_shouldReturnPendingRequest() {
        Long userId = 1L;
        Long eventId = 10L;
        User requester = new User(); requester.setId(userId);
        User initiator = new User(); initiator.setId(2L);

        Event event = new Event();
        event.setId(eventId);
        event.setInitiator(initiator);
        event.setState(EventState.PUBLISHED);
        event.setRequestModeration(true);
        event.setParticipantLimit(10);

        when(userService.getUser(userId)).thenReturn(requester);
        when(eventService.getEventWithLock(eventId)).thenReturn(event);
        when(requestRepository.existsByEventIdAndRequesterId(eventId, userId)).thenReturn(false);
        when(requestRepository.save(any(Request.class))).thenAnswer(i -> i.getArgument(0));

        Request savedRequest = requestService.createRequest(userId, eventId);

        assertThat(savedRequest.getStatus()).isEqualTo(RequestStatus.PENDING);
        assertThat(savedRequest.getRequester()).isEqualTo(requester);
        assertThat(savedRequest.getEvent()).isEqualTo(event);
    }

    @Test
    void createRequest_whenModerationFalse_shouldReturnConfirmedRequest() {
        Long userId = 1L;
        Long eventId = 10L;
        User requester = new User(); requester.setId(userId);
        User initiator = new User(); initiator.setId(2L);

        Event event = new Event();
        event.setId(eventId);
        event.setInitiator(initiator);
        event.setState(EventState.PUBLISHED);
        event.setRequestModeration(false);
        event.setParticipantLimit(15);

        when(userService.getUser(userId)).thenReturn(requester);
        when(eventService.getEventWithLock(eventId)).thenReturn(event);
        when(requestRepository.existsByEventIdAndRequesterId(eventId, userId)).thenReturn(false);
        when(requestRepository.save(any(Request.class))).thenAnswer(i -> i.getArgument(0));

        Request savedRequest = requestService.createRequest(userId, eventId);

        assertThat(savedRequest.getStatus()).isEqualTo(RequestStatus.CONFIRMED);
    }

    @Test
    void createRequest_whenUserNotFound_shouldThrowNotFoundException() {
        when(userService.getUser(99L))
                .thenThrow(new NotFoundException("User with id=99 was not found"));

        assertThatThrownBy(() -> requestService.createRequest(99L, 1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User with id=99 was not found");
    }

    @Test
    void createRequest_whenInitiatorRequestsOwnEvent_shouldThrowConflictException() {
        Long userId = 1L;
        User user = new User(); user.setId(userId);
        Event event = new Event(); event.setId(10L); event.setInitiator(user);

        when(userService.getUser(userId)).thenReturn(user);
        when(eventService.getEventWithLock(10L)).thenReturn(event);

        assertThatThrownBy(() -> requestService.createRequest(userId, 10L))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Initiator cannot add a request to participate in his event");
    }

    @Test
    void createRequest_whenEventNotPublished_shouldThrowConflictException() {
        Long userId = 1L;
        User user = new User(); user.setId(userId);
        User initiator = new User(); initiator.setId(2L);
        Event event = new Event(); event.setId(10L); event.setInitiator(initiator); event.setState(EventState.PENDING);

        when(userService.getUser(userId)).thenReturn(user);
        when(eventService.getEventWithLock(10L)).thenReturn(event);

        assertThatThrownBy(() -> requestService.createRequest(userId, 10L))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Cannot participate in an unpublished event");
    }

    @Test
    void createRequest_whenDuplicate_shouldThrowConflictException() {
        Long userId = 1L;
        User user = new User(); user.setId(userId);
        User initiator = new User(); initiator.setId(2L);
        Event event = new Event(); event.setId(10L); event.setInitiator(initiator); event.setState(EventState.PUBLISHED);

        when(userService.getUser(userId)).thenReturn(user);
        when(eventService.getEventWithLock(10L)).thenReturn(event);
        when(requestRepository.existsByEventIdAndRequesterId(10L, userId)).thenReturn(true);

        assertThatThrownBy(() -> requestService.createRequest(userId, 10L))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Request already exists");
    }

    @Test
    void createRequest_whenLimitReached_shouldThrowConflictException() {
        Long userId = 1L;
        User user = new User(); user.setId(userId);
        User initiator = new User(); initiator.setId(2L);

        Event event = new Event();
        event.setId(10L);
        event.setInitiator(initiator);
        event.setState(EventState.PUBLISHED);
        event.setParticipantLimit(5); // Лимит 5 человек

        when(userService.getUser(userId)).thenReturn(user);
        when(eventService.getEventWithLock(10L)).thenReturn(event);
        when(requestRepository.existsByEventIdAndRequesterId(10L, userId)).thenReturn(false);
        when(requestRepository.countByEventIdAndStatus(10L, RequestStatus.CONFIRMED)).thenReturn(5L);

        assertThatThrownBy(() -> requestService.createRequest(userId, 10L))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Participant limit has been reached");
    }


    @Test
    void cancelRequest_whenValid_shouldChangeStatusToCanceled() {
        Long userId = 1L;
        Request request = new Request();
        request.setId(5L);
        request.setRequester(new User()); request.getRequester().setId(userId);
        request.setStatus(RequestStatus.PENDING);

        when(requestRepository.findById(5L)).thenReturn(Optional.of(request));

        Request canceledRequest = requestService.cancelRequest(userId, 5L);

        assertThat(canceledRequest.getStatus()).isEqualTo(RequestStatus.CANCELED);
    }

    @Test
    void cancelRequest_whenRequestNotFound_shouldThrowNotFoundException() {
        when(requestRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> requestService.cancelRequest(1L, 99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Request with id=99 was not found");
    }

    @Test
    void cancelRequest_whenNotOwner_shouldThrowNotFoundException() {
        Request request = new Request();
        request.setId(5L);
        request.setRequester(new User()); request.getRequester().setId(2L);

        when(requestRepository.findById(5L)).thenReturn(Optional.of(request));

        assertThatThrownBy(() -> requestService.cancelRequest(1L, 5L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("was not found for user 1");
    }

    @Test
    void cancelRequest_whenStatusIsNotPending_shouldThrowConflictException() {
        Long userId = 1L;
        Request request = new Request();
        request.setId(5L);
        request.setRequester(new User()); request.getRequester().setId(userId);
        request.setStatus(RequestStatus.CONFIRMED);

        when(requestRepository.findById(5L)).thenReturn(Optional.of(request));

        assertThatThrownBy(() -> requestService.cancelRequest(userId, 5L))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Only pending requests can be canceled");
    }

    @Test
    void getEventRequests_shouldReturnEventRequests() {
        Request request = new Request();
        when(requestRepository.findAllByEventId(10L)).thenReturn(List.of(request));

        assertThat(requestService.getEventRequests(10L)).containsExactly(request);
    }

    @Test
    void getRequestsByIds_shouldReturnRequestsByIds() {
        Request request = new Request();
        when(requestRepository.findAllByIdIn(List.of(1L, 2L))).thenReturn(List.of(request));

        assertThat(requestService.getRequestsByIds(List.of(1L, 2L))).containsExactly(request);
    }

    @Test
    void getEventRequestsByStatus_shouldReturnRequestsWithStatus() {
        Request request = new Request();
        when(requestRepository.findAllByEventIdInAndStatus(List.of(10L), RequestStatus.PENDING)).thenReturn(List.of(request));

        assertThat(requestService.getEventRequestsByStatus(List.of(10L), RequestStatus.PENDING)).containsExactly(request);
    }

    @Test
    void countConfirmedRequests_shouldCountConfirmedRequests() {
        when(requestRepository.countByEventIdAndStatus(10L, RequestStatus.CONFIRMED)).thenReturn(3L);

        assertThat(requestService.countConfirmedRequests(10L)).isEqualTo(3L);
    }
}
