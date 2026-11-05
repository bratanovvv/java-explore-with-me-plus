package ru.practicum.request.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import ru.practicum.category.model.Category;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventState;
import ru.practicum.event.model.Location;
import ru.practicum.request.model.Request;
import ru.practicum.request.model.RequestStatus;
import ru.practicum.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class RequestRepositoryTest {

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void findAllByEventId_shouldReturnOnlyEventRequestsWithFetchedRelations() {
        Event event = persistEvent("Первое");
        Event otherEvent = persistEvent("Второе");
        Request request = persistRequest(event, persistUser("Первый"), RequestStatus.PENDING);
        persistRequest(otherEvent, persistUser("Второй"), RequestStatus.PENDING);
        flushAndClear();

        List<Request> requests = requestRepository.findAllByEventId(event.getId());

        assertThat(requests).extracting(Request::getId).containsExactly(request.getId());
        assertThat(isLoaded(requests.getFirst(), "event")).isTrue();
        assertThat(isLoaded(requests.getFirst(), "requester")).isTrue();
    }

    @Test
    void findAllByIdIn_shouldReturnRequestedRequests() {
        Event event = persistEvent("Первое");
        Request first = persistRequest(event, persistUser("Первый"), RequestStatus.PENDING);
        Request second = persistRequest(event, persistUser("Второй"), RequestStatus.PENDING);
        persistRequest(event, persistUser("Третий"), RequestStatus.PENDING);
        flushAndClear();

        List<Request> requests = requestRepository.findAllByIdIn(List.of(first.getId(), second.getId()));

        assertThat(requests).extracting(Request::getId)
                .containsExactlyInAnyOrder(first.getId(), second.getId());
        assertThat(isLoaded(requests.getFirst(), "requester")).isTrue();
    }

    @Test
    void findAllByEventIdInAndStatus_shouldReturnRequestsOfGivenEventsAndStatus() {
        Event first = persistEvent("Первое");
        Event second = persistEvent("Второе");
        Event third = persistEvent("Третье");
        Request firstConfirmed = persistRequest(first, persistUser("Первый"), RequestStatus.CONFIRMED);
        Request secondConfirmed = persistRequest(second, persistUser("Второй"), RequestStatus.CONFIRMED);
        persistRequest(first, persistUser("Третий"), RequestStatus.PENDING);
        persistRequest(third, persistUser("Четвёртый"), RequestStatus.CONFIRMED);
        flushAndClear();

        List<Request> requests = requestRepository.findAllByEventIdInAndStatus(
                List.of(first.getId(), second.getId()), RequestStatus.CONFIRMED);

        assertThat(requests).extracting(Request::getId)
                .containsExactlyInAnyOrder(firstConfirmed.getId(), secondConfirmed.getId());
    }

    @Test
    void updateStatusByIdsInEvent_shouldUpdateOnlyGivenRequestsOfEvent() {
        Event event = persistEvent("Первое");
        Event otherEvent = persistEvent("Второе");
        Request first = persistRequest(event, persistUser("Первый"), RequestStatus.PENDING);
        Request second = persistRequest(event, persistUser("Второй"), RequestStatus.PENDING);
        Request foreign = persistRequest(otherEvent, persistUser("Третий"), RequestStatus.PENDING);
        flushAndClear();

        requestRepository.updateStatusByIdsInEvent(
                List.of(first.getId(), second.getId()), RequestStatus.CONFIRMED, event.getId());

        assertThat(requestRepository.findById(first.getId()).orElseThrow().getStatus())
                .isEqualTo(RequestStatus.CONFIRMED);
        assertThat(requestRepository.findById(second.getId()).orElseThrow().getStatus())
                .isEqualTo(RequestStatus.CONFIRMED);
        assertThat(requestRepository.findById(foreign.getId()).orElseThrow().getStatus())
                .isEqualTo(RequestStatus.PENDING);
    }

    @Test
    void updateStatusByEventAndStatus_shouldUpdateAllMatchingRequests() {
        Event event = persistEvent("Первое");
        persistRequest(event, persistUser("Первый"), RequestStatus.PENDING);
        persistRequest(event, persistUser("Второй"), RequestStatus.PENDING);
        persistRequest(event, persistUser("Третий"), RequestStatus.CONFIRMED);
        flushAndClear();

        int updated = requestRepository.updateStatusByEventAndStatus(
                event.getId(), RequestStatus.PENDING, RequestStatus.REJECTED);

        assertThat(updated).isEqualTo(2);
        assertThat(requestRepository.findAllByEventId(event.getId()))
                .extracting(Request::getStatus)
                .containsExactlyInAnyOrder(
                        RequestStatus.CONFIRMED, RequestStatus.REJECTED, RequestStatus.REJECTED);
    }

    private Event persistEvent(String title) {
        Category category = entityManager.persist(Category.builder().name(title + "-category").build());
        User initiator = entityManager.persist(User.builder()
                .name(title + "-initiator")
                .email(title.toLowerCase() + "-initiator@mail.ru")
                .build());
        return entityManager.persist(Event.builder()
                .annotation("Аннотация события " + title)
                .category(category)
                .description("Описание события " + title)
                .eventDate(LocalDateTime.now().plusDays(5))
                .createdOn(LocalDateTime.now())
                .initiator(initiator)
                .location(new Location(55.75, 37.62))
                .paid(false)
                .participantLimit(10)
                .requestModeration(true)
                .state(EventState.PUBLISHED)
                .title(title)
                .build());
    }

    private User persistUser(String name) {
        return entityManager.persist(User.builder()
                .name(name)
                .email(name.toLowerCase() + "@mail.ru")
                .build());
    }

    private Request persistRequest(Event event, User requester, RequestStatus status) {
        return entityManager.persist(Request.builder()
                .event(event)
                .requester(requester)
                .created(LocalDateTime.now())
                .status(status)
                .build());
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    private boolean isLoaded(Request request, String attribute) {
        return entityManager.getEntityManager()
                .getEntityManagerFactory()
                .getPersistenceUnitUtil()
                .isLoaded(request, attribute);
    }
}
