package ru.practicum.event.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventState;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {

    @Override
    @EntityGraph(attributePaths = {"category", "initiator"})
    Optional<Event> findById(Long id);

    @EntityGraph(attributePaths = {"category", "initiator"})
    Optional<Event> findByIdAndState(Long id, EventState state);

    @EntityGraph(attributePaths = {"category", "initiator"})
    Optional<Event> findByIdAndInitiatorId(Long id, Long initiatorId);

    @EntityGraph(attributePaths = {"category", "initiator"})
    Page<Event> findAllByInitiatorId(Long initiatorId, Pageable pageable);

    /**
     * Публичный поиск событий.
     * <p>
     * TODO: фильтр {@code onlyAvailable} — {@code e.participantLimit = 0} OR {@code (select count(r) from ParticipationRequest r where r.event.id = e.id and r.status = 'CONFIRMED') < e.participantLimit}.
     */
    @Query("""
            select e from Event e
            where e.state = :state
              and (:paid is null or e.paid = :paid)
              and (:filterCategories = false or e.category.id in :categories)
              and (:text is null
                   or lower(e.annotation) like concat('%', lower(:text), '%')
                   or lower(e.description) like concat('%', lower(:text), '%'))
              and (:rangeStart is null or e.eventDate >= :rangeStart)
              and (:rangeEnd is null or e.eventDate <= :rangeEnd)
            """)
    @EntityGraph(attributePaths = {"category", "initiator"})
    List<Event> findPublicEvents(@Param("state") EventState state,
                                 @Param("text") String text,
                                 @Param("categories") List<Long> categories,
                                 @Param("filterCategories") boolean filterCategories,
                                 @Param("paid") Boolean paid,
                                 @Param("rangeStart") LocalDateTime rangeStart,
                                 @Param("rangeEnd") LocalDateTime rangeEnd,
                                 Pageable pageable);

    /**
     * Административный поиск событий.
     */
    @Query("""
            select e from Event e
            where (:filterUsers = false or e.initiator.id in :users)
              and (:filterStates = false or e.state in :states)
              and (:filterCategories = false or e.category.id in :categories)
              and (:rangeStart is null or e.eventDate >= :rangeStart)
              and (:rangeEnd is null or e.eventDate <= :rangeEnd)
            """)
    @EntityGraph(attributePaths = {"category", "initiator"})
    List<Event> findAdminEvents(@Param("users") List<Long> users,
                                @Param("filterUsers") boolean filterUsers,
                                @Param("states") List<EventState> states,
                                @Param("filterStates") boolean filterStates,
                                @Param("categories") List<Long> categories,
                                @Param("filterCategories") boolean filterCategories,
                                @Param("rangeStart") LocalDateTime rangeStart,
                                @Param("rangeEnd") LocalDateTime rangeEnd,
                                Pageable pageable);

    default List<Event> findAdminEvents(List<Long> users,
                                        List<EventState> states,
                                        List<Long> categories,
                                        LocalDateTime rangeStart,
                                        LocalDateTime rangeEnd,
                                        Pageable pageable) {
        List<Long> usersList = safeList(users);
        List<EventState> statesList = safeList(states);
        List<Long> categoriesList = safeList(categories);
        return findAdminEvents(
                usersList, !usersList.isEmpty(),
                statesList, !statesList.isEmpty(),
                categoriesList, !categoriesList.isEmpty(),
                rangeStart, rangeEnd, pageable);
    }

    default List<Event> findPublicEvents(EventState state,
                                         String text,
                                         List<Long> categories,
                                         Boolean paid,
                                         LocalDateTime rangeStart,
                                         LocalDateTime rangeEnd,
                                         Pageable pageable) {
        List<Long> categoriesList = safeList(categories);
        return findPublicEvents(
                state, text, categoriesList, !categoriesList.isEmpty(), paid, rangeStart, rangeEnd, pageable);
    }

    private static <T> List<T> safeList(List<T> values) {
        return values == null ? List.of() : values;
    }
}
