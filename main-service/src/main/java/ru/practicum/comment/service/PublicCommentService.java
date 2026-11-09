package ru.practicum.comment.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.comment.dto.query.CommentSort;
import ru.practicum.comment.model.Comment;
import ru.practicum.comment.model.CommentStatus;
import ru.practicum.comment.repository.CommentRepository;
import ru.practicum.event.service.EventService;
import ru.practicum.utils.params.PageParams;

@Slf4j
@Service
@Transactional(readOnly = true)
public class PublicCommentService {

    private final CommentRepository commentRepository;
    private final EventService eventService;

    public PublicCommentService(CommentRepository commentRepository, EventService eventService) {
        this.commentRepository = commentRepository;
        this.eventService = eventService;
    }

    public Page<Comment> getPublishedComments(Long eventId, CommentSort sort, PageParams pageParams) {
        eventService.getPublicEvent(eventId);
        Sort.Direction direction = sort == CommentSort.OLDEST ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sortSpec = Sort.by(direction, "created", "id");
        return commentRepository.findAllByEventIdAndStatus(
                eventId, CommentStatus.PUBLISHED, pageParams.toPageable(sortSpec));
    }
}
