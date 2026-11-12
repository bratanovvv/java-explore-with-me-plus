package ru.practicum.comment.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.comment.dto.query.CommentSort;
import ru.practicum.comment.dto.request.CommentRequestDto;
import ru.practicum.comment.model.Comment;
import ru.practicum.comment.model.CommentStatus;
import ru.practicum.comment.repository.CommentRepository;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventState;
import ru.practicum.event.service.EventService;
import ru.practicum.user.model.User;
import ru.practicum.user.service.UserService;
import ru.practicum.utils.error.exception.ConflictException;
import ru.practicum.utils.error.exception.ForbiddenException;
import ru.practicum.utils.error.exception.NotFoundException;
import ru.practicum.utils.params.PageParams;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@Transactional(readOnly = true)
public class PrivateCommentService {

    private final EventService eventService;
    private final UserService userService;
    private final CommentRepository commentRepository;

    public PrivateCommentService(EventService eventService, UserService userService, CommentRepository commentRepository) {
        this.eventService = eventService;
        this.userService = userService;
        this.commentRepository = commentRepository;
    }

    @Transactional
    public Comment createComment(Long userId, Long eventId, CommentRequestDto dto) {
        User writer = userService.getUser(userId);
        Event event = eventService.getEvent(eventId);
        if (event.getState() == EventState.PENDING || event.getState() == EventState.CANCELED) {
            throw new ConflictException("Status must be PUBLISHED");
        }
        Comment comment = Comment.builder()
                .author(writer)
                .event(event)
                .status(CommentStatus.PENDING)
                .text(dto.getText())
                .build();

        Comment save = commentRepository.save(comment);
        log.debug("Saved comment = {}", save);
        return save;
    }

    @Transactional
    public void deleteCommentByUser(Long commentId, Long userId) {
        userService.getUser(userId);
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment with id = " + commentId + " was not found"));
        if (!Objects.equals(comment.getAuthor().getId(), userId)) {
            throw new ForbiddenException("Comment with id = " + commentId + " belongs to another user");
        }
        commentRepository.delete(comment);
        log.debug("Пользователь id={} удалил комментарий id={}", userId, commentId);
    }

    public List<Comment> getCommentsByUser(Long userId, PageParams params) {
        User author = userService.getUser(userId);
        Pageable pageable = params.toPageable(CommentSort.NEWEST.toSort());
        return commentRepository.findAllByAuthor(author, pageable);
    }
}
