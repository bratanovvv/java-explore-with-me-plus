package ru.practicum.comment.controller;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.query.CommentSort;
import ru.practicum.comment.mapper.CommentMapper;
import ru.practicum.comment.service.PublicCommentService;
import ru.practicum.utils.params.PageParams;

@Validated
@RestController
@RequestMapping("/comments")
public class PublicCommentController {

    private final PublicCommentService commentService;
    private final CommentMapper commentMapper;

    public PublicCommentController(PublicCommentService commentService, CommentMapper commentMapper) {
        this.commentService = commentService;
        this.commentMapper = commentMapper;
    }

    @GetMapping("/{eventId}")
    public Page<CommentDto> getComments(
            @PathVariable Long eventId,
            @RequestParam(defaultValue = CommentSort.DEFAULT_NAME) CommentSort sort,
            @RequestParam(defaultValue = PageParams.DEFAULT_FROM) @PositiveOrZero int from,
            @RequestParam(defaultValue = PageParams.DEFAULT_SIZE) @Positive int size) {
        return commentService.getPublishedComments(eventId, sort, new PageParams(from, size))
                .map(commentMapper::toDto);
    }
}
