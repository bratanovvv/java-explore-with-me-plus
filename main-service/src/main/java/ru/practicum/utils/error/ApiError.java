package ru.practicum.utils.error;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;
import ru.practicum.utils.formats.DateFormats;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Единое тело ответа при ошибке.
 */
@Data
@AllArgsConstructor
public class ApiError {

    @JsonFormat(pattern = DateFormats.DATE_TIME)
    private final LocalDateTime timestamp = LocalDateTime.now();

    private String status;
    private String reason;
    private String message;
    private List<String> errors;

    public static ApiError of(HttpStatus status, ErrorReason reason, String message) {
        return of(status, reason, message, List.of());
    }

    public static ApiError of(HttpStatus status, ErrorReason reason, String message, List<String> errors) {
        return new ApiError(status.name(), reason.getMessage(), message, errors);
    }
}
