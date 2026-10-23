package ru.practicum.utils.integrations;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.practicum.stats.client.StatsClient;
import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Интеграция основного сервиса с сервисом статистики.
 */
@Service
public class StatsService {

    private final StatsClient statsClient;
    private final String appName;

    public StatsService(StatsClient statsClient,
                        @Value("${spring.application.name}") String appName) {
        this.statsClient = statsClient;
        this.appName = appName;
    }

    /**
     * Сохраняет хит для текущего публичного запроса.
     *
     * @param request запрос, из которого берутся uri и ip
     */
    public void hit(HttpServletRequest request) {
        EndpointHitDto endpointHitDto = EndpointHitDto.builder()
                .app(appName)
                .uri(request.getRequestURI())
                .ip(request.getRemoteAddr())
                .timestamp(LocalDateTime.now())
                .build();
        statsClient.hit(endpointHitDto);
    }

    /**
     * Возвращает статистику просмотров за переданный период.
     *
     * @param start  начало периода
     * @param end    конец периода
     * @param uris   список uri; {@code null} или пустой — статистика по всем uri
     * @param unique учитывать только уникальные посещения
     * @return агрегированная статистика по каждому uri
     */
    public List<ViewStatsDto> getViews(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        return statsClient.getStats(start, end, uris, unique);
    }
}
