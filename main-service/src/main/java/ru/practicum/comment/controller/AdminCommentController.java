package ru.practicum.comment.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.request.CommentStatusUpdateRequest;
import ru.practicum.comment.mapper.CommentMapper;
import ru.practicum.comment.model.CommentStatus;
import ru.practicum.comment.service.AdminCommentService;
import ru.practicum.utils.params.PageParams;

@Slf4j
@Validated
@RestController
@RequestMapping("/comments/admin")
@RequiredArgsConstructor
public class AdminCommentController {

    private final AdminCommentService adminCommentService;
    private final CommentMapper commentMapper;

    @GetMapping
    public Page<CommentDto> getComments(
            @RequestParam(required = false) CommentStatus status,
            @RequestParam(required = false) Long eventId,
            @RequestParam(defaultValue = PageParams.DEFAULT_FROM) @PositiveOrZero int from,
            @RequestParam(defaultValue = PageParams.DEFAULT_SIZE) @Positive int size) {
        return adminCommentService.getComments(status, eventId, new PageParams(from, size))
                .map(commentMapper::toDto);
    }

    @PatchMapping("/{commentId}")
    public CommentDto updateStatus(
            @PathVariable Long commentId,
            @Valid @RequestBody CommentStatusUpdateRequest request) {
        return commentMapper.toDto(adminCommentService.updateStatus(commentId, request));
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long commentId) {
        adminCommentService.delete(commentId);
    }
}
