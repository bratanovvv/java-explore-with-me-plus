package ru.practicum.utils.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Общие причины ошибок для поля {@code reason} в {@link ApiError}.
 */
@Getter
@RequiredArgsConstructor
public enum ErrorReason {

    BAD_REQUEST("Incorrectly made request."),
    NOT_FOUND("The required object was not found."),
    CONFLICT("For the requested operation the conditions are not met."),
    INTERNAL_SERVER_ERROR("Unexpected error.");

    private final String message;
}
