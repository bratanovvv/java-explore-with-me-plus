package ru.practicum.request.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventState;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.request.model.Request;
import ru.practicum.request.model.RequestStatus;
import ru.practicum.request.repository.RequestRepository;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;
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
    private EventRepository eventRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RequestService requestService;

    @Test
    void getUserRequests_whenUserExists_shouldReturnList() {
        Long userId = 1L;
        Request request = new Request();
        when(userRepository.existsById(userId)).thenReturn(true);
        when(requestRepository.findAllByRequesterId(userId)).thenReturn(List.of(request));

        List<Request> result = requestService.getUserRequests(userId);

        assertThat(result).containsExactly(request);
    }

    @Test
    void getUserRequests_whenUserNotFound_shouldThrowNotFoundException() {
        when(userRepository.existsById(99L)).thenReturn(false);

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

        when(userRepository.findById(userId)).thenReturn(Optional.of(requester));
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
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

        when(userRepository.findById(userId)).thenReturn(Optional.of(requester));
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(requestRepository.existsByEventIdAndRequesterId(eventId, userId)).thenReturn(false);
        when(requestRepository.save(any(Request.class))).thenAnswer(i -> i.getArgument(0));

        Request savedRequest = requestService.createRequest(userId, eventId);

        assertThat(savedRequest.getStatus()).isEqualTo(RequestStatus.CONFIRMED);
    }

    @Test
    void createRequest_whenUserNotFound_shouldThrowNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> requestService.createRequest(99L, 1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User with id=99 was not found");
    }

    @Test
    void createRequest_whenInitiatorRequestsOwnEvent_shouldThrowConflictException() {
        Long userId = 1L;
        User user = new User(); user.setId(userId);
        Event event = new Event(); event.setId(10L); event.setInitiator(user); // Сам себе инициатор

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(eventRepository.findById(10L)).thenReturn(Optional.of(event));

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

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(eventRepository.findById(10L)).thenReturn(Optional.of(event));

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

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(eventRepository.findById(10L)).thenReturn(Optional.of(event));
        when(requestRepository.existsByEventIdAndRequesterId(10L, userId)).thenReturn(true); // Дубликат есть

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

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(eventRepository.findById(10L)).thenReturn(Optional.of(event));
        when(requestRepository.existsByEventIdAndRequesterId(10L, userId)).thenReturn(false);
        when(requestRepository.countByEventIdAndStatus(10L, RequestStatus.CONFIRMED)).thenReturn(5L); // Уже 5

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
        request.setRequester(new User()); request.getRequester().setId(2L); // Чужая заявка

        when(requestRepository.findById(5L)).thenReturn(Optional.of(request));

        assertThatThrownBy(() -> requestService.cancelRequest(1L, 5L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("was not found for user 1");
    }

    @Test
    void cancelRequest_whenStatusIsNotPending_shouldThrowConflictException() {
        // ВНИМАНИЕ: Этот тест упадет, пока ты не вернешь проверку статуса в сервис!
        Long userId = 1L;
        Request request = new Request();
        request.setId(5L);
        request.setRequester(new User()); request.getRequester().setId(userId);
        request.setStatus(RequestStatus.CONFIRMED); // Уже подтверждена

        when(requestRepository.findById(5L)).thenReturn(Optional.of(request));

        assertThatThrownBy(() -> requestService.cancelRequest(userId, 5L))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Only pending requests can be canceled");
    }
}
