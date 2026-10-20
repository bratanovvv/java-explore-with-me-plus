package ru.practicum.stats.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriBuilder;
import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
class StatsClientImpl implements StatsClient {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern(EndpointHitDto.DATE_PATTERN);

    private final RestClient restClient;

    StatsClientImpl(RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public void hit(EndpointHitDto endpointHitDto) {
        try {
            restClient.post()
                    .uri("/hit")
                    .body(endpointHitDto)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException e) {
            log.warn("Не удалось сохранить hit в сервисе статистики: " + e.getMessage());
        }
    }

    @Override
    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        try {
            List<ViewStatsDto> stats = restClient.get()
                    .uri(uriBuilder -> {
                        UriBuilder builder = uriBuilder
                                .path("/stats")
                                .queryParam("start", start.format(DATE_FORMATTER))
                                .queryParam("end", end.format(DATE_FORMATTER))
                                .queryParam("unique", unique);
                        if (uris != null && !uris.isEmpty()) {
                            builder = builder.queryParam("uris", String.join(",", uris));
                        }
                        return builder.build();
                    })
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<ViewStatsDto>>() {});
            return stats == null ? List.of() : stats;
        } catch (RestClientException e) {
            log.warn("Не удалось получить статистику из сервиса статистики: " + e.getMessage());
            return List.of();
        }
    }
}
