package ru.practicum.utils.error.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import ru.practicum.utils.error.ErrorReason;

/**
 * Базовое исключение сервиса.
 */
@Getter
public abstract class ServiceException extends RuntimeException {

    private final HttpStatus status;
    private final ErrorReason reason;

    protected ServiceException(HttpStatus status, ErrorReason reason, String message) {
        super(message);
        this.status = status;
        this.reason = reason;
    }
}
