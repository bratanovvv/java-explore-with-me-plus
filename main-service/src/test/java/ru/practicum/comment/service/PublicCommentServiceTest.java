package ru.practicum.comment.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.practicum.comment.dto.query.CommentSort;
import ru.practicum.comment.model.Comment;
import ru.practicum.comment.model.CommentStatus;
import ru.practicum.comment.repository.CommentRepository;
import ru.practicum.event.service.EventService;
import ru.practicum.utils.error.exception.NotFoundException;
import ru.practicum.utils.params.PageParams;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PublicCommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private EventService eventService;

    @InjectMocks
    private PublicCommentService publicCommentService;

    @Test
    void getPublishedComments_shouldFilterByPublishedStatusAndSortNewest() {
        Comment comment = Comment.builder().id(1L).build();
        Page<Comment> page = new PageImpl<>(List.of(comment));
        when(commentRepository.findAllByEventIdAndStatus(eq(7L), eq(CommentStatus.PUBLISHED), any(Pageable.class)))
                .thenReturn(page);

        Page<Comment> result = publicCommentService.getPublishedComments(7L, CommentSort.NEWEST,
                new PageParams(0, 10));

        verify(eventService).getPublicEvent(7L);
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(commentRepository).findAllByEventIdAndStatus(eq(7L), eq(CommentStatus.PUBLISHED),
                pageableCaptor.capture());
        assertThat(pageableCaptor.getValue().getOffset()).isZero();
        assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(10);
        assertThat(pageableCaptor.getValue().getSort()).isEqualTo(Sort.by(Sort.Direction.DESC, "created")
                .and(Sort.by(Sort.Direction.DESC, "id")));
        assertThat(result.getContent()).containsExactly(comment);
    }

    @Test
    void getPublishedComments_shouldSortOldest() {
        when(commentRepository.findAllByEventIdAndStatus(eq(7L), eq(CommentStatus.PUBLISHED), any(Pageable.class)))
                .thenReturn(Page.empty());

        publicCommentService.getPublishedComments(7L, CommentSort.OLDEST, new PageParams(5, 3));

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(commentRepository).findAllByEventIdAndStatus(eq(7L), eq(CommentStatus.PUBLISHED),
                pageableCaptor.capture());
        assertThat(pageableCaptor.getValue().getOffset()).isEqualTo(5);
        assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(3);
        assertThat(pageableCaptor.getValue().getSort()).isEqualTo(Sort.by(Sort.Direction.ASC, "created")
                .and(Sort.by(Sort.Direction.ASC, "id")));
    }

    @Test
    void getPublishedComments_shouldPropagateNotFoundForUnpublishedEvent() {
        when(eventService.getPublicEvent(99L))
                .thenThrow(new NotFoundException("Event with id=99 was not found"));

        assertThatThrownBy(() -> publicCommentService.getPublishedComments(99L, CommentSort.NEWEST,
                new PageParams(0, 10)))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Event with id=99 was not found");

        verify(commentRepository, never()).findAllByEventIdAndStatus(any(), any(), any());
    }
}
