package ru.practicum.comment.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.model.Comment;

import java.util.List;

@Component
public class CommentMapper {

    public CommentDto toDto(Comment comment) {
        if (comment == null) return null;

        return CommentDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .eventId(comment.getEvent().getId())
                .authorId(comment.getAuthor().getId())
                .created(comment.getCreated())
                .status(comment.getStatus())
                .build();
    }

    public List<CommentDto> toDtoList(List<Comment> comments) {
        return comments.stream()
                .map(this::toDto)
                .toList();
    }
}

