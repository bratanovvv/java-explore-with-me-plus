package ru.practicum.utils.integrations;

import ru.practicum.stats.dto.ViewStatsDto;
import ru.practicum.utils.params.PageParams;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Базовая реализация обогащения DTO данными сервиса статистики.
 * <p>
 * Наследник описывает, как получить DTO, URI сущности, как проставить просмотры и как их сравнивать.
 * Статистика запрашивается одним батчем на весь список сущностей.
 *
 * @param <E> тип сущности
 * @param <D> тип DTO
 */
public abstract class StatsEnricher<E, D> {

    private static final LocalDateTime STATS_START = LocalDateTime.of(1970, 1, 1, 0, 0);

    private final StatsService statsService;

    protected StatsEnricher(StatsService statsService) {
        this.statsService = statsService;
    }

    public D toDto(E entity) {
        return toDtos(List.of(entity)).getFirst();
    }

    public List<D> toDtos(List<E> entities) {
        if (entities.isEmpty()) {
            return List.of();
        }
        Map<String, Long> views = loadViews(entities);
        return entities.stream()
                .map(entity -> {
                    D dto = map(entity);
                    applyViews(dto, views.getOrDefault(uri(entity), 0L));
                    return dto;
                })
                .toList();
    }

    public List<D> toDtosByViews(List<E> entities, PageParams pageParams) {
        List<D> sortedDtos = toDtos(entities).stream()
                .sorted(viewsComparator())
                .toList();
        int from = pageParams.from();
        if (from >= sortedDtos.size()) {
            return List.of();
        }
        int to = Math.min(from + pageParams.size(), sortedDtos.size());
        return sortedDtos.subList(from, to);
    }

    private Map<String, Long> loadViews(List<E> entities) {
        List<String> uris = entities.stream()
                .map(this::uri)
                .toList();
        List<ViewStatsDto> stats = statsService.getViews(STATS_START, LocalDateTime.now(), uris, true);
        return stats.stream()
                .collect(Collectors.toMap(
                        ViewStatsDto::getUri,
                        ViewStatsDto::getHits,
                        (first, second) -> first));
    }

    protected abstract D map(E entity);

    protected abstract String uri(E entity);

    protected abstract void applyViews(D dto, long views);

    protected abstract Comparator<D> viewsComparator();
}
