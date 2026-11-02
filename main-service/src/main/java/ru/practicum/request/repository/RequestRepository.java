package ru.practicum.request.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.request.model.Request;
import ru.practicum.request.model.RequestStatus;

import java.util.List;

public interface RequestRepository extends JpaRepository<Request, Long> {

    @EntityGraph(attributePaths = {"event", "requester"})
    List<Request> findAllByRequesterId(Long requesterId);

    boolean existsByEventIdAndRequesterId(Long eventId, Long requesterId);

    long countByEventIdAndStatus(Long eventId, RequestStatus status);
}
