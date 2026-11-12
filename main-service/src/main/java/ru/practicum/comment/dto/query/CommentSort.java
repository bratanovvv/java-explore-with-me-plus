package ru.practicum.comment.dto.query;

import org.springframework.data.domain.Sort;


public enum CommentSort {

    NEWEST(Sort.Direction.DESC),
    OLDEST(Sort.Direction.ASC);

    public static final String DEFAULT_NAME = "NEWEST";

    private static final String FIELD_CREATED = "created";
    private static final String FIELD_ID = "id";

    private final Sort.Direction direction;

    CommentSort(Sort.Direction direction) {
        this.direction = direction;
    }

    public Sort toSort() {
        return Sort.by(direction, FIELD_CREATED, FIELD_ID);
    }
}
