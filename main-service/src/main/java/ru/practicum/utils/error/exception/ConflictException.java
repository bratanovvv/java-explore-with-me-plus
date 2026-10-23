package ru.practicum.utils.error.exception;

import org.springframework.http.HttpStatus;
import ru.practicum.utils.error.ErrorReason;

/**
 * Нарушение бизнес-правила или целостности данных (HTTP 409).
 */
public class ConflictException extends ServiceException {

    public ConflictException(String message) {
        super(HttpStatus.CONFLICT, ErrorReason.CONFLICT, message);
    }
}
