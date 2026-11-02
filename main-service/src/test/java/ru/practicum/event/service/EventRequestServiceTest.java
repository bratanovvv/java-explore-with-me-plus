package ru.practicum.event.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventState;
import ru.practicum.request.dto.RequestDto;
import ru.practicum.request.dto.RequestStatusUpdate;
import ru.practicum.request.dto.RequestStatusUpdateResult;
import ru.practicum.request.mapper.RequestMapper;
import ru.practicum.request.model.Request;
import ru.practicum.request.model.RequestStatus;
import ru.practicum.request.service.RequestService;
import ru.practicum.user.model.User;
import ru.practicum.utils.error.exception.BadRequestException;
import ru.practicum.utils.error.exception.ConflictException;
import ru.practicum.utils.error.exception.NotFoundException;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventRequestServiceTest {

    private static final Long OWNER_ID = 1L;
    private static final Long EVENT_ID = 10L;

    @Mock
    private EventService eventService;

    @Mock
    private RequestService requestService;

    private EventRequestService eventRequestService;

    @BeforeEach
    void setUp() {
        eventRequestService = new EventRequestService(
                eventService, requestService, new EventValidator(), new RequestMapper());
    }

    @Test
    void getEventRequests_whenEventOwnedByUser_shouldReturnRequests() {
        List<Request> requests = List.of(buildRequest(1L, RequestStatus.PENDING));
        when(eventService.getUserEvent(OWNER_ID, EVENT_ID)).thenReturn(buildEvent(10, true));
        when(requestService.getEventRequests(EVENT_ID)).thenReturn(requests);

        List<Request> result = eventRequestService.getEventRequests(OWNER_ID, EVENT_ID);

        assertThat(result).containsExactlyElementsOf(requests);
    }

    @Test
    void getEventRequests_whenEventNotFound_shouldThrowNotFoundException() {
        when(eventService.getUserEvent(OWNER_ID, EVENT_ID))
                .thenThrow(new NotFoundException("Event with id=10 was not found"));

        assertThatThrownBy(() -> eventRequestService.getEventRequests(OWNER_ID, EVENT_ID))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Event with id=10 was not found");
    }

    @Test
    void changeRequestStatus_whenStatusIsNotSupported_shouldThrowBadRequestException() {
        RequestStatusUpdate update = buildUpdate(RequestStatus.PENDING, 1L);

        assertThatThrownBy(() -> eventRequestService.changeRequestStatus(OWNER_ID, EVENT_ID, update))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Request status must be CONFIRMED or REJECTED");
        verifyNoInteractions(eventService, requestService);
    }

    @Test
    void changeRequestStatus_whenEventBelongsToAnotherUser_shouldThrowNotFoundException() {
        RequestStatusUpdate update = buildUpdate(RequestStatus.CONFIRMED, 1L);
        Event event = buildEvent(10, true);
        event.setInitiator(User.builder().id(2L).build());
        when(eventService.getEventWithLock(EVENT_ID)).thenReturn(event);

        assertThatThrownBy(() -> eventRequestService.changeRequestStatus(OWNER_ID, EVENT_ID, update))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Event with id=10 was not found");
    }

    @Test
    void changeRequestStatus_whenParticipantLimitIsZero_shouldThrowConflictException() {
        RequestStatusUpdate update = buildUpdate(RequestStatus.CONFIRMED, 1L);
        when(eventService.getEventWithLock(EVENT_ID)).thenReturn(buildEvent(0, true));

        assertThatThrownBy(() -> eventRequestService.changeRequestStatus(OWNER_ID, EVENT_ID, update))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("does not require requests confirmation");
        verifyNoInteractions(requestService);
    }

    @Test
    void changeRequestStatus_whenModerationDisabled_shouldThrowConflictException() {
        RequestStatusUpdate update = buildUpdate(RequestStatus.CONFIRMED, 1L);
        when(eventService.getEventWithLock(EVENT_ID)).thenReturn(buildEvent(10, false));

        assertThatThrownBy(() -> eventRequestService.changeRequestStatus(OWNER_ID, EVENT_ID, update))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("does not require requests confirmation");
        verifyNoInteractions(requestService);
    }

    @Test
    void changeRequestStatus_whenRequestNotFound_shouldThrowNotFoundException() {
        RequestStatusUpdate update = buildUpdate(RequestStatus.CONFIRMED, 1L, 2L);
        when(eventService.getEventWithLock(EVENT_ID)).thenReturn(buildEvent(10, true));
        when(requestService.getRequestsByIds(List.of(1L, 2L)))
                .thenReturn(List.of(buildRequest(1L, RequestStatus.PENDING)));

        assertThatThrownBy(() -> eventRequestService.changeRequestStatus(OWNER_ID, EVENT_ID, update))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Request with id=2 was not found");
    }

    @Test
    void changeRequestStatus_whenRequestBelongsToAnotherEvent_shouldThrowNotFoundException() {
        RequestStatusUpdate update = buildUpdate(RequestStatus.REJECTED, 1L);
        Request request = buildRequest(1L, RequestStatus.PENDING);
        request.setEvent(Event.builder().id(99L).build());
        when(eventService.getEventWithLock(EVENT_ID)).thenReturn(buildEvent(10, true));
        when(requestService.getRequestsByIds(List.of(1L))).thenReturn(List.of(request));

        assertThatThrownBy(() -> eventRequestService.changeRequestStatus(OWNER_ID, EVENT_ID, update))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Request with id=1 was not found");
    }

    @Test
    void changeRequestStatus_whenRequestIsNotPending_shouldThrowConflictException() {
        RequestStatusUpdate update = buildUpdate(RequestStatus.REJECTED, 1L);
        when(eventService.getEventWithLock(EVENT_ID)).thenReturn(buildEvent(10, true));
        when(requestService.getRequestsByIds(List.of(1L)))
                .thenReturn(List.of(buildRequest(1L, RequestStatus.CONFIRMED)));

        assertThatThrownBy(() -> eventRequestService.changeRequestStatus(OWNER_ID, EVENT_ID, update))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Request must have status PENDING");
    }

    @Test
    void changeRequestStatus_whenRejecting_shouldRejectPendingRequests() {
        RequestStatusUpdate update = buildUpdate(RequestStatus.REJECTED, 1L, 2L);
        Request first = buildRequest(1L, RequestStatus.PENDING);
        Request second = buildRequest(2L, RequestStatus.PENDING);
        when(eventService.getEventWithLock(EVENT_ID)).thenReturn(buildEvent(10, true));
        when(requestService.getRequestsByIds(List.of(1L, 2L))).thenReturn(List.of(first, second));

        RequestStatusUpdateResult result = eventRequestService.changeRequestStatus(OWNER_ID, EVENT_ID, update);

        assertThat(result.confirmedRequests()).isEmpty();
        assertThat(result.rejectedRequests()).extracting(RequestDto::getId).containsExactly(1L, 2L);
        assertThat(result.rejectedRequests()).extracting(RequestDto::getStatus)
                .containsOnly(RequestStatus.REJECTED);
        verify(requestService).getRequestsByIds(List.of(1L, 2L));
    }

    @Test
    void changeRequestStatus_whenConfirming_shouldConfirmPendingRequest() {
        RequestStatusUpdate update = buildUpdate(RequestStatus.CONFIRMED, 1L);
        Request request = buildRequest(1L, RequestStatus.PENDING);
        when(eventService.getEventWithLock(EVENT_ID)).thenReturn(buildEvent(10, true));
        when(requestService.getRequestsByIds(List.of(1L))).thenReturn(List.of(request));
        when(requestService.countConfirmedRequests(EVENT_ID)).thenReturn(0L);

        RequestStatusUpdateResult result = eventRequestService.changeRequestStatus(OWNER_ID, EVENT_ID, update);

        assertThat(result.confirmedRequests()).extracting(RequestDto::getId).containsExactly(1L);
        assertThat(result.rejectedRequests()).isEmpty();
        assertThat(request.getStatus()).isEqualTo(RequestStatus.CONFIRMED);
    }

    @Test
    void changeRequestStatus_whenParticipantLimitAlreadyReached_shouldThrowConflictException() {
        RequestStatusUpdate update = buildUpdate(RequestStatus.CONFIRMED, 1L);
        when(eventService.getEventWithLock(EVENT_ID)).thenReturn(buildEvent(1, true));
        when(requestService.getRequestsByIds(List.of(1L)))
                .thenReturn(List.of(buildRequest(1L, RequestStatus.PENDING)));
        when(requestService.countConfirmedRequests(EVENT_ID)).thenReturn(1L);

        assertThatThrownBy(() -> eventRequestService.changeRequestStatus(OWNER_ID, EVENT_ID, update))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("The participant limit has been reached");
    }

    @Test
    void changeRequestStatus_whenLimitExhaustedAfterConfirmation_shouldRejectRemainingPending() {
        RequestStatusUpdate update = buildUpdate(RequestStatus.CONFIRMED, 1L, 2L);
        Request first = buildRequest(1L, RequestStatus.PENDING);
        Request second = buildRequest(2L, RequestStatus.PENDING);
        Request remaining = buildRequest(3L, RequestStatus.PENDING);
        when(eventService.getEventWithLock(EVENT_ID)).thenReturn(buildEvent(2, true));
        when(requestService.getRequestsByIds(List.of(1L, 2L))).thenReturn(List.of(first, second));
        when(requestService.countConfirmedRequests(EVENT_ID)).thenReturn(1L);
        when(requestService.getEventRequestsByStatus(List.of(EVENT_ID), RequestStatus.PENDING))
                .thenReturn(List.of(remaining));

        RequestStatusUpdateResult result = eventRequestService.changeRequestStatus(OWNER_ID, EVENT_ID, update);

        assertThat(result.confirmedRequests()).extracting(RequestDto::getId).containsExactly(1L);
        assertThat(result.rejectedRequests()).extracting(RequestDto::getId).containsExactly(2L, 3L);
        assertThat(second.getStatus()).isEqualTo(RequestStatus.REJECTED);
        assertThat(remaining.getStatus()).isEqualTo(RequestStatus.REJECTED);
    }

    private RequestStatusUpdate buildUpdate(RequestStatus status, Long... requestIds) {
        RequestStatusUpdate update = new RequestStatusUpdate();
        update.setStatus(status);
        update.setRequestIds(List.of(requestIds));
        return update;
    }

    private Event buildEvent(int participantLimit, boolean requestModeration) {
        return Event.builder()
                .id(EVENT_ID)
                .initiator(User.builder().id(OWNER_ID).build())
                .participantLimit(participantLimit)
                .requestModeration(requestModeration)
                .state(EventState.PUBLISHED)
                .build();
    }

    private Request buildRequest(Long id, RequestStatus status) {
        return Request.builder()
                .id(id)
                .event(Event.builder().id(EVENT_ID).build())
                .requester(User.builder().id(100L + id).build())
                .created(LocalDateTime.now())
                .status(status)
                .build();
    }
}
