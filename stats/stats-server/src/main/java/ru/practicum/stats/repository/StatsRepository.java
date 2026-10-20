package ru.practicum.stats.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.stats.model.EndpointHit;
import ru.practicum.stats.model.projection.ViewStats;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsRepository extends JpaRepository<EndpointHit, Long> {

    @Query("select new ru.practicum.stats.projection.ViewStats(h.app, h.uri, count(h)) "
            + "from EndpointHit h "
            + "where h.timestamp between :start and :end "
            + "group by h.app, h.uri "
            + "order by count(h) desc")
    List<ViewStats> findStats(@Param("start") LocalDateTime start,
                              @Param("end") LocalDateTime end);

    @Query("select new ru.practicum.stats.projection.ViewStats(h.app, h.uri, count(h)) "
            + "from EndpointHit h "
            + "where h.timestamp between :start and :end "
            + "and h.uri in :uris "
            + "group by h.app, h.uri "
            + "order by count(h) desc")
    List<ViewStats> findStatsByUris(@Param("start") LocalDateTime start,
                                    @Param("end") LocalDateTime end,
                                    @Param("uris") List<String> uris);

    @Query("select new ru.practicum.stats.projection.ViewStats(h.app, h.uri, count(distinct h.ip)) "
            + "from EndpointHit h "
            + "where h.timestamp between :start and :end "
            + "group by h.app, h.uri "
            + "order by count(distinct h.ip) desc")
    List<ViewStats> findUniqueStats(@Param("start") LocalDateTime start,
                                    @Param("end") LocalDateTime end);

    @Query("select new ru.practicum.stats.projection.ViewStats(h.app, h.uri, count(distinct h.ip)) "
            + "from EndpointHit h "
            + "where h.timestamp between :start and :end "
            + "and h.uri in :uris "
            + "group by h.app, h.uri "
            + "order by count(distinct h.ip) desc")
    List<ViewStats> findUniqueStatsByUris(@Param("start") LocalDateTime start,
                                          @Param("end") LocalDateTime end,
                                          @Param("uris") List<String> uris);
}
