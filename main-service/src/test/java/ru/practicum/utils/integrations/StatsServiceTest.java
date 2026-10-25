package ru.practicum.utils.integrations;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import ru.practicum.stats.client.StatsClient;
import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatsServiceTest {

    private static final String APP_NAME = "ewm-main-service";

    @Mock
    private StatsClient statsClient;

    private StatsService statsService;

    @BeforeEach
    void setUp() {
        statsService = new StatsService(statsClient, APP_NAME);
    }

    @Test
    void hit_shouldSendEndpointHit() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/events/1");
        request.setRemoteAddr("127.0.0.1");

        statsService.hit(request);

        ArgumentCaptor<EndpointHitDto> hitCaptor = ArgumentCaptor.forClass(EndpointHitDto.class);
        verify(statsClient).hit(hitCaptor.capture());
        EndpointHitDto hit = hitCaptor.getValue();
        assertThat(hit.getApp()).isEqualTo(APP_NAME);
        assertThat(hit.getUri()).isEqualTo("/events/1");
        assertThat(hit.getIp()).isEqualTo("127.0.0.1");
        assertThat(hit.getTimestamp()).isNotNull();
    }

    @Test
    void getViews_shouldReturnStatsFromClient() {
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now();
        List<String> uris = List.of("/events/1");
        List<ViewStatsDto> expected = List.of(new ViewStatsDto(APP_NAME, "/events/1", 5L));
        when(statsClient.getStats(start, end, uris, true)).thenReturn(expected);

        List<ViewStatsDto> actual = statsService.getViews(start, end, uris, true);

        assertThat(actual).isEqualTo(expected);
        verify(statsClient).getStats(start, end, uris, true);
    }
}