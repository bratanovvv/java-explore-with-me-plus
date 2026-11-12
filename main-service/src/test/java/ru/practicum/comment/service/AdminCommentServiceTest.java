package ru.practicum.comment.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import ru.practicum.comment.dto.request.CommentStatusUpdateRequest;
import ru.practicum.comment.mapper.CommentMapper;
import ru.practicum.comment.model.Comment;
import ru.practicum.comment.model.CommentStatus;
import ru.practicum.comment.repository.CommentRepository;
import ru.practicum.utils.error.exception.BadRequestException;
import ru.practicum.utils.error.exception.ConflictException;
import ru.practicum.utils.error.exception.NotFoundException;
import ru.practicum.utils.params.PageParams;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminCommentServiceTest {

    @Mock
    private CommentRepository commentRepository;
    @Mock
    private CommentMapper commentMapper;

    @InjectMocks
    private AdminCommentService adminCommentService;

    @Test
    void getComments_shouldReturnPage() {
        Comment comment = new Comment();
        when(commentRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(comment)));

        var result = adminCommentService.getComments(CommentStatus.PENDING, 1L, new PageParams(0, 10));

        assertThat(result.getContent()).hasSize(1);
        verify(commentRepository).findAll(any(org.springframework.data.jpa.domain.Specification.class), any(Pageable.class));
    }

    @Test
    void updateStatus_toPublished_shouldSucceed() {
        Comment comment = Comment.builder().id(1L).status(CommentStatus.PENDING).build();
        CommentStatusUpdateRequest request = new CommentStatusUpdateRequest();
        request.setStatus(CommentStatus.PUBLISHED);

        when(commentRepository.findByIdWithLock(1L)).thenReturn(Optional.of(comment));

        Comment result = adminCommentService.updateStatus(1L, request);

        assertThat(result.getStatus()).isEqualTo(CommentStatus.PUBLISHED);
        assertThat(result.getRejectionReason()).isNull();
    }

    @Test
    void updateStatus_toRejected_shouldSaveReason() {
        Comment comment = Comment.builder().id(1L).status(CommentStatus.PENDING).build();
        CommentStatusUpdateRequest request = new CommentStatusUpdateRequest();
        request.setStatus(CommentStatus.REJECTED);
        request.setRejectionReason("Спам");

        when(commentRepository.findByIdWithLock(1L)).thenReturn(Optional.of(comment));

        Comment result = adminCommentService.updateStatus(1L, request);

        assertThat(result.getStatus()).isEqualTo(CommentStatus.REJECTED);
        assertThat(result.getRejectionReason()).isEqualTo("Спам");
    }

    @Test
    void updateStatus_whenNotPending_shouldThrowConflict() {
        Comment comment = Comment.builder().id(1L).status(CommentStatus.PUBLISHED).build(); // Уже опубликован
        CommentStatusUpdateRequest request = new CommentStatusUpdateRequest();
        request.setStatus(CommentStatus.REJECTED);

        when(commentRepository.findByIdWithLock(1L)).thenReturn(Optional.of(comment));

        assertThatThrownBy(() -> adminCommentService.updateStatus(1L, request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Only comments with status PENDING can be moderated");
    }

    @Test
    void updateStatus_toInvalidStatus_shouldThrowBadRequest() {
        CommentStatusUpdateRequest request = new CommentStatusUpdateRequest();
        request.setStatus(CommentStatus.PENDING);

        assertThatThrownBy(() -> adminCommentService.updateStatus(1L, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Target status must be PUBLISHED or REJECTED");
    }

    @Test
    void updateStatus_whenNotFound_shouldThrowNotFound() {
        CommentStatusUpdateRequest request = new CommentStatusUpdateRequest();
        request.setStatus(CommentStatus.PUBLISHED);

        when(commentRepository.findByIdWithLock(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminCommentService.updateStatus(99L, request))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void delete_shouldRemoveComment() {
        Comment comment = Comment.builder().id(1L).build();
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));

        adminCommentService.delete(1L);

        verify(commentRepository).delete(comment);
    }

    @Test
    void delete_whenNotFound_shouldThrowNotFound() {
        when(commentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminCommentService.delete(99L))
                .isInstanceOf(NotFoundException.class);
    }
}

