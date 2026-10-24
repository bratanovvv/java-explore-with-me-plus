package ru.practicum.utils.params;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

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
        return toPageable(Sort.unsorted());
    }

    /**
     * Страница загружается с запасом, чтобы компенсировать некратность {@code from} размеру страницы.
     * Лишние элементы, загруженные из-за смещения внутри страницы, отбрасывает {@link #cut(List)}.
     */
    public Pageable toPageable(Sort sort) {
        return PageRequest.of(pageNumber(), size + offsetInPage(), sort);
    }

    /**
     * Отбрасывает элементы, попавшие в выборку из-за некратности {@code from} размеру страницы,
     * и оставляет ровно {@code size} элементов, начиная с {@code from}.
     *
     * @param content элементы загруженной страницы
     * @param <T>     тип элементов
     * @return запрошенное окно элементов
     */
    public <T> List<T> cut(List<T> content) {
        return content.stream()
                .skip(offsetInPage())
                .limit(size)
                .toList();
    }

    private int pageNumber() {
        return from / size;
    }

    private int offsetInPage() {
        return from % size;
    }
}
