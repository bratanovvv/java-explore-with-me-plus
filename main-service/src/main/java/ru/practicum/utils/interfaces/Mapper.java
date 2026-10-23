package ru.practicum.utils.interfaces;

import java.util.List;

/**
 * Контракт маппера между объектом и DTO.
 * <p>
 * Маппер реализует {@link #toDto(Object)} и {@link #toEntity(Object)},
 * преобразование списков предоставляется по умолчанию.
 *
 * @param <E> тип объекта
 * @param <D> тип DTO
 */
public interface Mapper<E, D> {

    D toDto(E entity);

    E toEntity(D dto);

    default List<D> toDtoList(List<E> entities) {
        return entities.stream()
                .map(this::toDto)
                .toList();
    }
}
