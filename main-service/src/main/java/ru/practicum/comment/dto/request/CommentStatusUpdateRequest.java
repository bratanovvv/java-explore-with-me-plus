package ru.practicum.comment.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import ru.practicum.comment.model.CommentStatus;

@Data
public class CommentStatusUpdateRequest {

    @NotNull
    private CommentStatus status;
    @Size(max = 500)
    private String rejectionReason;
}