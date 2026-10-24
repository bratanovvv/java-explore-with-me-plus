package ru.practicum.utils.error.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import ru.practicum.utils.error.ErrorReason;

import static org.assertj.core.api.Assertions.assertThat;

class BadRequestExceptionTest {

    @Test
    void shouldCarryBadRequestStatusAndReason() {
        BadRequestException exception = new BadRequestException("Invalid request");

        assertThat(exception.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(exception.getReason()).isEqualTo(ErrorReason.BAD_REQUEST);
        assertThat(exception.getMessage()).isEqualTo("Invalid request");
    }
}
