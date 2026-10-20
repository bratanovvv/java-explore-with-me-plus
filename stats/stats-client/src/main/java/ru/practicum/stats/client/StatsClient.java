package ru.practicum.stats.client;

import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Контракт HTTP-клиента сервиса статистики.
 * <p>
 * Методы принимают и возвращают только DTO, поэтому потребитель не зависит от
 * конкретной реализации и способа передачи данных.
 */
public interface StatsClient {

    /**
     * Сохраняет информацию о том, что к эндпоинту был запрос.
     *
     * @param endpointHitDto данные запроса
     */
    void hit(EndpointHitDto endpointHitDto);

    /**
     * Возвращает статистику посещений за указанный период.
     *
     * @param start  начало периода
     * @param end    конец периода
     * @param uris   список uri, по которым нужна статистика (может быть {@code null})
     * @param unique учитывать только уникальные посещения
     * @return агрегированная статистика по каждому uri
     */
    List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique);
}
