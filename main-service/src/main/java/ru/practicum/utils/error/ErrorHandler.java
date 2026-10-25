package ru.practicum.utils.error;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import ru.practicum.utils.error.exception.ServiceException;

import java.util.List;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    /**
     * Бизнес-исключения сервиса.
     */
    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<ApiError> handleServiceException(ServiceException exception) {
        HttpStatus status = exception.getStatus();
        log.warn("{} {}: {}", status.value(), status.getReasonPhrase(), exception.getMessage());
        return ResponseEntity.status(status)
                .body(ApiError.of(status, exception.getReason(), exception.getMessage()));
    }

    /**
     * Ошибки Bean Validation.
     */
    @ExceptionHandler({MethodArgumentNotValidException.class, ConstraintViolationException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidationException(Exception exception) {
        List<String> errors = extractValidationErrors(exception);
        String message = "Validation failed. Error count: " + errors.size();
        log.warn("400 Bad Request: {}", errors);
        return ApiError.of(HttpStatus.BAD_REQUEST, ErrorReason.BAD_REQUEST, message, errors);
    }

    /**
     * Прочие некорректные запросы: битый JSON, ошибки валидации параметров метода контроллера.
     */
    @ExceptionHandler({
            HandlerMethodValidationException.class,
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class,
            HttpMessageNotReadableException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleBadRequest(Exception exception) {
        String message = resolveBadRequestMessage(exception);
        log.warn("400 Bad Request: {}", message);
        return ApiError.of(HttpStatus.BAD_REQUEST, ErrorReason.BAD_REQUEST, message);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleDataIntegrityViolation(DataIntegrityViolationException exception) {
        String message = exception.getMostSpecificCause().getMessage();
        log.warn("409 Conflict: {}", exception.getMostSpecificCause().getMessage());
        return ApiError.of(HttpStatus.CONFLICT, ErrorReason.CONFLICT, ErrorReason.CONFLICT.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleException(Exception exception) {
        log.error("500 Internal Server Error", exception);
        return ApiError.of(HttpStatus.INTERNAL_SERVER_ERROR, ErrorReason.INTERNAL_SERVER_ERROR,
                exception.getMessage());
    }

    private List<String> extractValidationErrors(Exception exception) {
        if (exception instanceof MethodArgumentNotValidException methodArgumentNotValid) {
            return methodArgumentNotValid.getBindingResult().getFieldErrors().stream()
                    .map(error -> error.getField() + ": " + error.getDefaultMessage())
                    .toList();
        }
        if (exception instanceof ConstraintViolationException constraintViolation) {
            return constraintViolation.getConstraintViolations().stream()
                    .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                    .toList();
        }
        return List.of();
    }

    private String resolveBadRequestMessage(Exception exception) {
        if (exception instanceof MissingServletRequestParameterException missingParameter) {
            return "Required parameter '" + missingParameter.getParameterName() + "' is missing";
        }
        if (exception instanceof MethodArgumentTypeMismatchException typeMismatch) {
            String requiredType =
                    typeMismatch.getRequiredType() == null ? "unknown" : typeMismatch.getRequiredType().getSimpleName();
            return "Failed to convert value " + typeMismatch.getValue() + " to required type " + requiredType;
        }
        if (exception instanceof HttpMessageNotReadableException) {
            return "Malformed JSON request";
        }
        return exception.getMessage() == null ? "Bad request" : exception.getMessage();
    }
}
