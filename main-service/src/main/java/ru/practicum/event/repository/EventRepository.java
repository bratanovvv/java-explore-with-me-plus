package ru.practicum.event.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventState;

import java.util.List;
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

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Event e where e.id = :id")
    Optional<Event> findByIdWithLock(@Param("id") Long id);

    @Override
    @EntityGraph(attributePaths = {"category", "initiator"})
    List<Event> findAllById(Iterable<Long> ids);
}
