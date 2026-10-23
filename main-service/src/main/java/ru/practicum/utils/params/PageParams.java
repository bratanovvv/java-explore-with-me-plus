package ru.practicum.utils.params;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Параметры пагинации списочных эндпоинтов.
 * <p>
 * {@code from} — смещение (количество пропускаемых элементов), {@code size} — размер страницы.
 * Значения по умолчанию: {@code from=0}, {@code size=10}.
 */
public record PageParams(int from, int size) {

    public static final String DEFAULT_FROM = "0";

    public static final String DEFAULT_SIZE = "10";

    public PageParams {
        if (from < 0) {
            throw new IllegalArgumentException("from must be greater than or equal to 0");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("size must be greater than 0");
        }
    }

    public Pageable toPageable() {
        return PageRequest.of(pageNumber(), size);
    }

    public Pageable toPageable(Sort sort) {
        return PageRequest.of(pageNumber(), size, sort);
    }

    private int pageNumber() {
        return from / size;
    }
}
