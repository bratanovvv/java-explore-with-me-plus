package ru.practicum.request.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ru.practicum.request.model.RequestStatus;

import java.util.List;

/**
 * Новый статус для заявок на участие в событии текущего пользователя.
 */
@Data
public class RequestStatusUpdate {

    @NotEmpty
    private List<Long> requestIds;

    @NotNull
    private RequestStatus status;
}
