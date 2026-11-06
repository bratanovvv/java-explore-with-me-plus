package ru.practicum.utils.error.exception;

import org.springframework.http.HttpStatus;
import ru.practicum.utils.error.ErrorReason;

/**
 * Сущность не найдена или недоступна (HTTP 404).
 */
public class NotFoundException extends ServiceException {

    public NotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, ErrorReason.NOT_FOUND, message);
    }
}
