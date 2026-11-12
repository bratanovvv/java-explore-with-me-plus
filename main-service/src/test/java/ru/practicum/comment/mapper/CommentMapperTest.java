package ru.practicum.comment.mapper;

import org.junit.jupiter.api.Test;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.model.Comment;
import ru.practicum.comment.model.CommentStatus;
import ru.practicum.event.model.Event;
import ru.practicum.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CommentMapperTest {

    private final CommentMapper commentMapper = new CommentMapper();

    @Test
    void toDto_shouldMapAllFields() {
        LocalDateTime created = LocalDateTime.of(2026, 11, 8, 20, 10);
        Comment comment = Comment.builder()
                .id(5L)
                .text("Отличное событие")
                .event(Event.builder().id(1L).build())
                .author(User.builder().id(2L).build())
                .created(created)
                .status(CommentStatus.REJECTED)
                .rejectionReason("Содержит спам")
                .build();

        CommentDto dto = commentMapper.toDto(comment);

        assertThat(dto.getId()).isEqualTo(5L);
        assertThat(dto.getText()).isEqualTo("Отличное событие");
        assertThat(dto.getEventId()).isEqualTo(1L);
        assertThat(dto.getAuthorId()).isEqualTo(2L);
        assertThat(dto.getCreated()).isEqualTo(created);
        assertThat(dto.getStatus()).isEqualTo(CommentStatus.REJECTED);
        assertThat(dto.getRejectionReason()).isEqualTo("Содержит спам");
    }

    @Test
    void toDto_shouldReturnNullForNullComment() {
        assertThat(commentMapper.toDto(null)).isNull();
    }

    @Test
    void toDtoList_shouldMapList() {
        Comment first = Comment.builder().id(1L).event(Event.builder().id(10L).build())
                .author(User.builder().id(20L).build()).build();
        Comment second = Comment.builder().id(2L).event(Event.builder().id(10L).build())
                .author(User.builder().id(21L).build()).build();

        List<CommentDto> result = commentMapper.toDtoList(List.of(first, second));

        assertThat(result).extracting(CommentDto::getId).containsExactly(1L, 2L);
    }

    @Test
    void toDtoList_shouldReturnEmptyList() {
        assertThat(commentMapper.toDtoList(List.of())).isEmpty();
    }

    @Test
    void toEntity_shouldMapFieldsAndReferences() {
        LocalDateTime created = LocalDateTime.of(2026, 11, 8, 20, 10);
        CommentDto dto = CommentDto.builder()
                .id(5L)
                .text("Отличное событие")
                .eventId(1L)
                .authorId(2L)
                .created(created)
                .status(CommentStatus.REJECTED)
                .rejectionReason("Содержит спам")
                .build();

        Comment comment = commentMapper.toEntity(dto);

        assertThat(comment.getId()).isEqualTo(5L);
        assertThat(comment.getText()).isEqualTo("Отличное событие");
        assertThat(comment.getEvent().getId()).isEqualTo(1L);
        assertThat(comment.getAuthor().getId()).isEqualTo(2L);
        assertThat(comment.getCreated()).isEqualTo(created);
        assertThat(comment.getStatus()).isEqualTo(CommentStatus.REJECTED);
        assertThat(comment.getRejectionReason()).isEqualTo("Содержит спам");
    }

    @Test
    void toEntity_shouldReturnNullForNullDto() {
        assertThat(commentMapper.toEntity(null)).isNull();
    }
}
