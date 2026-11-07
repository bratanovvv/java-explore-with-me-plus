package ru.practicum.comment.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.comment.model.CommentStatus;

import java.time.LocalDateTime;

@Data
@Builder
public class CommentDto {
    private Long id;
    private String text;
    private Long eventId;
    private Long authorId;
    private LocalDateTime created;
    private CommentStatus status;
}
