package ru.practicum.comment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.Builder;
import lombok.Data;
import ru.practicum.comment.model.CommentStatus;
import ru.practicum.utils.formats.DateFormats;

import java.time.LocalDateTime;

@Data
@Builder
public class CommentDto {

    public interface PublicComment {
    }

    public interface ModeratedComment extends PublicComment {
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    private String text;

    private Long eventId;

    private Long authorId;

    @JsonFormat(pattern = DateFormats.DATE_TIME)
    private LocalDateTime created;

    @JsonView(ModeratedComment.class)
    private CommentStatus status;

    @JsonView(ModeratedComment.class)
    private String rejectionReason;
}
