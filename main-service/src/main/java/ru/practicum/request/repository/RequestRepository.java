package ru.practicum.request.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update Request r set r.status = :status where r.id in :ids and r.event.id = :eventId")
    int updateStatusByIdsInEvent(@Param("ids") Collection<Long> ids,
                                 @Param("status") RequestStatus status,
                                 @Param("eventId") Long eventId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update Request r set r.status = :newStatus "
            + "where r.event.id = :eventId and r.status = :oldStatus")
    int updateStatusByEventAndStatus(@Param("eventId") Long eventId,
                                     @Param("oldStatus") RequestStatus oldStatus,
                                     @Param("newStatus") RequestStatus newStatus);
}
