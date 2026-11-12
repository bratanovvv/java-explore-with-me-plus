package ru.practicum.comment.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.model.Comment;
import ru.practicum.event.model.Event;
import ru.practicum.user.model.User;
import ru.practicum.utils.interfaces.Mapper;

@Component
public class CommentMapper implements Mapper<Comment, CommentDto> {

    @Override
    public CommentDto toDto(Comment comment) {
        if (comment == null) {
            return null;
        }

        return CommentDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .eventId(comment.getEvent().getId())
                .authorId(comment.getAuthor().getId())
                .created(comment.getCreated())
                .status(comment.getStatus())
                .rejectionReason(comment.getRejectionReason())
                .build();
    }

    @Override
    public Comment toEntity(CommentDto dto) {
        if (dto == null) {
            return null;
        }

        return Comment.builder()
                .id(dto.getId())
                .text(dto.getText())
                .event(toEventReference(dto.getEventId()))
                .author(toUserReference(dto.getAuthorId()))
                .created(dto.getCreated())
                .status(dto.getStatus())
                .rejectionReason(dto.getRejectionReason())
                .build();
    }

    private Event toEventReference(Long eventId) {
        if (eventId == null) {
            return null;
        }
        return Event.builder().id(eventId).build();
    }

    private User toUserReference(Long authorId) {
        if (authorId == null) {
            return null;
        }
        return User.builder().id(authorId).build();
    }
}
