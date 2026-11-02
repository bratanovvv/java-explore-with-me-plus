package ru.practicum.request.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.request.model.Request;
import ru.practicum.request.model.RequestStatus;

import java.util.Collection;
import java.util.List;

public interface RequestRepository extends JpaRepository<Request, Long> {

    @EntityGraph(attributePaths = {"event", "requester"})
    List<Request> findAllByRequesterId(Long requesterId);

    @EntityGraph(attributePaths = {"event", "requester"})
    List<Request> findAllByEventId(Long eventId);

    @EntityGraph(attributePaths = {"event", "requester"})
    List<Request> findAllByIdIn(Collection<Long> ids);

    List<Request> findAllByEventIdInAndStatus(Collection<Long> eventIds, RequestStatus status);

    boolean existsByEventIdAndRequesterId(Long eventId, Long requesterId);

    long countByEventIdAndStatus(Long eventId, RequestStatus status);
}
