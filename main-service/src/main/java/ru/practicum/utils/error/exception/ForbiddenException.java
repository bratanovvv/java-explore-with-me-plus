package ru.practicum.utils.error.exception;

import org.springframework.http.HttpStatus;
import ru.practicum.utils.error.ErrorReason;

public class ForbiddenException extends ServiceException {

    public ForbiddenException(String message) {
        super(HttpStatus.FORBIDDEN, ErrorReason.FORBIDDEN, message);
    }
}
