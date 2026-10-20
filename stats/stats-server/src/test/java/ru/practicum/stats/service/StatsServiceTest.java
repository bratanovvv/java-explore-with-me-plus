package ru.practicum.stats.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.stats.model.EndpointHit;
import ru.practicum.stats.model.projection.ViewStats;
import ru.practicum.stats.repository.StatsRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatsServiceTest {

    private static final LocalDateTime START = LocalDateTime.of(2026, 10, 20, 0, 0, 0);
    private static final LocalDateTime END = LocalDateTime.of(2026, 10, 21, 0, 0, 0);

    @Mock
    private StatsRepository statsRepository;

    @InjectMocks
    private StatsService statsService;

    @Test
    void saveHit_shouldSaveEntity() {
        EndpointHit hit = new EndpointHit();

        statsService.saveHit(hit);

        verify(statsRepository).save(hit);
    }

    @Test
    void getStats_withoutUrisAndNotUnique_shouldCallFindStats() {
        List<ViewStats> stats = List.of(new ViewStats("app", "/uri", 3L));
        when(statsRepository.findStats(START, END)).thenReturn(stats);

        List<ViewStats> result = statsService.getStats(START, END, null, false);

        verify(statsRepository).findStats(START, END);
        assertThat(result).isEqualTo(stats);
    }

    @Test
    void getStats_withoutUrisAndUnique_shouldCallFindUniqueStats() {
        List<ViewStats> stats = List.of(new ViewStats("app", "/uri", 2L));
        when(statsRepository.findUniqueStats(START, END)).thenReturn(stats);

        List<ViewStats> result = statsService.getStats(START, END, List.of(), true);

        verify(statsRepository).findUniqueStats(START, END);
        assertThat(result).isEqualTo(stats);
    }

    @Test
    void getStats_withUrisAndNotUnique_shouldCallFindStatsByUris() {
        List<String> uris = List.of("/uri");
        when(statsRepository.findStatsByUris(START, END, uris)).thenReturn(List.of());

        statsService.getStats(START, END, uris, false);

        verify(statsRepository).findStatsByUris(START, END, uris);
    }

    @Test
    void getStats_withUrisAndUnique_shouldCallFindUniqueStatsByUris() {
        List<String> uris = List.of("/uri");
        when(statsRepository.findUniqueStatsByUris(START, END, uris)).thenReturn(List.of());

        statsService.getStats(START, END, uris, true);

        verify(statsRepository).findUniqueStatsByUris(START, END, uris);
    }
}
