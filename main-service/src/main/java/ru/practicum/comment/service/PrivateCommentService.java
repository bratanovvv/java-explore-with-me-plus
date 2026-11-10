package ru.practicum.comment.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.comment.dto.CommentRequestDto;
import ru.practicum.comment.model.Comment;
import ru.practicum.comment.model.CommentStatus;
import ru.practicum.comment.repository.CommentRepository;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventState;
import ru.practicum.event.service.EventService;
import ru.practicum.user.model.User;
import ru.practicum.user.service.UserService;
import ru.practicum.utils.error.exception.NotFoundException;
import ru.practicum.utils.params.PageParams;

import java.time.LocalDateTime;
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
        if (event.getState() == EventState.PENDING) {
            throw new NotFoundException("Status must be PUBLISHED");
        }
        Comment comment = Comment.builder()
                .author(writer)
                .event(event)
                .created(LocalDateTime.now())
                .status(CommentStatus.PENDING)
                .text(dto.getText())
                .build();

        Comment save = commentRepository.save(comment);
        log.debug("Saved comment = {}", save);
        return save;
    }


    public void deleteCommentByUser(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment with id = " + commentId + " was not found"));
        if (Objects.equals(comment.getAuthor().getId(), userId)) {
            commentRepository.delete(comment);
        } else {
            throw new NotFoundException("You can only delete your own comments.");
        }
    }

    public List<Comment> getCommentByUser(Long userId, PageParams params) {
        User author = userService.getUser(userId);
        Pageable pageable = params.toPageable(Sort.by(Sort.Direction.DESC, "created", "id"));
        return commentRepository.findAllByAuthor(author, pageable);
    }
}
