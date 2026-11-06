package ru.practicum.request.dto;

import java.util.List;

/**
 * Результат подтверждения/отклонения заявок на участие в событии.
 */
public record RequestStatusUpdateResult(List<RequestDto> confirmedRequests, List<RequestDto> rejectedRequests) {
}
