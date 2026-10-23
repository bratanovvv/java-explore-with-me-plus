package ru.practicum.utils.error.exception;

import org.springframework.http.HttpStatus;
import ru.practicum.utils.error.ErrorReason;

/**
 * Некорректный запрос, который не ловится Bean Validation (HTTP 400).
 */
public class BadRequestException extends ServiceException {

    public BadRequestException(String message) {
        super(HttpStatus.BAD_REQUEST, ErrorReason.BAD_REQUEST, message);
    }
}
