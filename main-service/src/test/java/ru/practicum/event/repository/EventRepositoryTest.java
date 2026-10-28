package ru.practicum.event.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.practicum.category.model.Category;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventState;
import ru.practicum.event.model.Location;
import ru.practicum.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class EventRepositoryTest {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void findById_shouldFetchCategoryAndInitiatorEagerly() {
        Event event = persistEvent("Событие", EventState.PENDING, true);
        Long eventId = event.getId();
        entityManager.flush();
        entityManager.clear();

        Event found = eventRepository.findById(eventId).orElseThrow();

        assertThat(isLoaded(found, "category")).isTrue();
        assertThat(isLoaded(found, "initiator")).isTrue();
    }

    @Test
    void listQueries_shouldFetchCategoryAndInitiatorEagerly() {
        Event event = persistEvent("Событие", EventState.PUBLISHED, true);
        Long eventId = event.getId();
        Long initiatorId = event.getInitiator().getId();
        entityManager.flush();
        entityManager.clear();

        List<Event> publicEvents = eventRepository.findPublicEvents(
                EventState.PUBLISHED, null, List.of(), false, null, null, null, Pageable.unpaged());
        List<Event> adminEvents = eventRepository.findAdminEvents(
                List.of(initiatorId), true, List.of(), false, List.of(), false, null, null, Pageable.unpaged());
        List<Event> byInitiator = eventRepository
                .findAllByInitiatorId(initiatorId, PageRequest.of(0, 10)).getContent();
        Optional<Event> byIdAndInitiator = eventRepository.findByIdAndInitiatorId(eventId, initiatorId);

        assertThat(publicEvents).hasSize(1);
        assertThat(isLoaded(publicEvents.getFirst(), "category")).isTrue();
        assertThat(isLoaded(adminEvents.getFirst(), "initiator")).isTrue();
        assertThat(isLoaded(byInitiator.getFirst(), "category")).isTrue();
        assertThat(byIdAndInitiator).isPresent();
        assertThat(isLoaded(byIdAndInitiator.orElseThrow(), "initiator")).isTrue();
    }

    @Test
    void findPublicEvents_shouldApplyAllFilters() {
        Event matching = persistEvent("Опубликованное платное", EventState.PUBLISHED, true);
        Long categoryId = matching.getCategory().getId();
        persistEvent("Опубликованное бесплатное", EventState.PUBLISHED, false);
        persistEvent("Черновик", EventState.PENDING, true);
        entityManager.flush();
        entityManager.clear();

        List<Event> events = eventRepository.findPublicEvents(
                EventState.PUBLISHED,
                "Опубликованное",
                List.of(categoryId),
                true,
                true,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(10),
                Pageable.unpaged());

        assertThat(events).extracting(Event::getTitle).containsExactly("Опубликованное платное");
    }

    @Test
    void findPublicEvents_shouldReturnOnlyPublishedWithoutFilters() {
        persistEvent("Опубликованное", EventState.PUBLISHED, true);
        persistEvent("Черновик", EventState.PENDING, true);
        entityManager.flush();
        entityManager.clear();

        List<Event> events = eventRepository.findPublicEvents(
                EventState.PUBLISHED, null, List.of(), false, null, null, null, Pageable.unpaged());

        assertThat(events).extracting(Event::getTitle).containsExactly("Опубликованное");
    }

    @Test
    void findPublicEvents_shouldApplyPaginationAndSorting() {
        persistEvent("Первое", EventState.PUBLISHED, true);
        persistEvent("Второе", EventState.PUBLISHED, true);
        persistEvent("Третье", EventState.PUBLISHED, true);
        entityManager.flush();
        entityManager.clear();

        List<Event> firstPage = eventRepository.findPublicEvents(
                EventState.PUBLISHED, null, List.of(), false, null, null, null,
                PageRequest.of(0, 2, Sort.by(Sort.Direction.ASC, "id")));

        assertThat(firstPage).hasSize(2);
        assertThat(firstPage).extracting(Event::getId).isSorted();
    }

    @Test
    void findAdminEvents_shouldApplyAllFilters() {
        Event matching = persistEvent("Админ-событие", EventState.PENDING, true);
        Long userId = matching.getInitiator().getId();
        Long categoryId = matching.getCategory().getId();
        persistEvent("Другое", EventState.PUBLISHED, false);
        entityManager.flush();
        entityManager.clear();

        List<Event> events = eventRepository.findAdminEvents(
                List.of(userId),
                true,
                List.of(EventState.PENDING),
                true,
                List.of(categoryId),
                true,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(10),
                Pageable.unpaged());

        assertThat(events).extracting(Event::getTitle).containsExactly("Админ-событие");
    }

    @Test
    void findAdminEvents_shouldReturnAllWhenFiltersDisabled() {
        persistEvent("Первое", EventState.PUBLISHED, true);
        persistEvent("Второе", EventState.PENDING, false);
        entityManager.flush();
        entityManager.clear();

        List<Event> events = eventRepository.findAdminEvents(
                List.of(), false, List.of(), false, List.of(), false, null, null, Pageable.unpaged());

        assertThat(events).hasSize(2);
    }

    private Event persistEvent(String title, EventState state, boolean paid) {
        Category category = entityManager.persist(Category.builder().name(title + "-category").build());
        User initiator = entityManager.persist(User.builder()
                .name(title + "-user")
                .email(title.toLowerCase() + "@mail.ru")
                .build());
        return entityManager.persist(Event.builder()
                .annotation("Аннотация события " + title)
                .category(category)
                .description("Описание события " + title)
                .eventDate(LocalDateTime.now().plusDays(5))
                .createdOn(LocalDateTime.now())
                .initiator(initiator)
                .location(new Location(55.75, 37.62))
                .paid(paid)
                .participantLimit(0)
                .requestModeration(true)
                .state(state)
                .title(title)
                .build());
    }

    private boolean isLoaded(Event event, String attribute) {
        return entityManager.getEntityManager()
                .getEntityManagerFactory()
                .getPersistenceUnitUtil()
                .isLoaded(event, attribute);
    }
}
