package ru.practicum.event.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventState;

import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {

    @Override
    @EntityGraph(attributePaths = {"category", "initiator"})
    Optional<Event> findById(Long id);

    @Override
    @EntityGraph(attributePaths = {"category", "initiator"})
    Page<Event> findAll(Specification<Event> spec, Pageable pageable);

    @EntityGraph(attributePaths = {"category", "initiator"})
    Optional<Event> findByIdAndState(Long id, EventState state);

    @EntityGraph(attributePaths = {"category", "initiator"})
    Page<Event> findAllByInitiatorId(Long initiatorId, Pageable pageable);

    @EntityGraph(attributePaths = {"category", "initiator"})
    Optional<Event> findByIdAndInitiatorId(Long id, Long initiatorId);
}
