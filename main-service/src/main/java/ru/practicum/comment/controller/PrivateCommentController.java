package ru.practicum.comment.controller;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.request.CommentRequestDto;
import ru.practicum.comment.mapper.CommentMapper;
import ru.practicum.comment.service.PrivateCommentService;
import ru.practicum.utils.params.PageParams;

@Slf4j
@Validated
@RestController
@RequestMapping("/users/{userId}/comments")
public class PrivateCommentController {

    private final PrivateCommentService commentService;
    private final CommentMapper commentMapper;

    public PrivateCommentController(PrivateCommentService commentService, CommentMapper commentMapper) {
        this.commentService = commentService;
        this.commentMapper = commentMapper;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @JsonView(CommentDto.ModeratedComment.class)
    public CommentDto createComment(@PathVariable Long userId,
                                    @RequestParam(name = "eventId", required = true) Long eventId,
                                    @RequestBody @Valid CommentRequestDto dto) {
        log.debug("Создание комментария: userId={}, eventId={}", userId, eventId);
        return commentMapper.toDto(commentService.createComment(userId, eventId, dto));
    }

    @GetMapping
    @JsonView(CommentDto.ModeratedComment.class)
    public Page<CommentDto> getComments(@PathVariable Long userId,
                                        @RequestParam(defaultValue = PageParams.DEFAULT_FROM) @PositiveOrZero int from,
                                        @RequestParam(defaultValue = PageParams.DEFAULT_SIZE) @Positive int size) {
        log.debug("Получение комментариев пользователя: userId={}, from={}, size={}", userId, from, size);
        return commentService.getCommentsByUser(userId, new PageParams(from, size))
                .map(commentMapper::toDto);
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable Long commentId, @PathVariable Long userId) {
        log.debug("Удаление комментария: userId={}, commentId={}", userId, commentId);
        commentService.deleteCommentByUser(commentId, userId);
    }
}
