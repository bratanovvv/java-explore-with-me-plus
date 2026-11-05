package ru.practicum.stats.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.stats.model.EndpointHit;
import ru.practicum.stats.model.projection.ViewStats;
import ru.practicum.stats.repository.StatsRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatsService {

    private final StatsRepository statsRepository;

    @Transactional
    public void saveHit(EndpointHit hit) {
        statsRepository.save(hit);
    }

    @Transactional(readOnly = true)
    public List<ViewStats> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        if (start.isAfter(end)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "start must be before or equal to end");
        }
        if (uris == null || uris.isEmpty()) {
            return unique ? statsRepository.findUniqueStats(start, end) : statsRepository.findStats(start, end);
        }
        return unique
                ? statsRepository.findUniqueStatsByUris(start, end, uris)
                : statsRepository.findStatsByUris(start, end, uris);
    }
}
