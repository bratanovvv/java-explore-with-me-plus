package ru.practicum.comment.service;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.comment.dto.request.CommentStatusUpdateRequest;
import ru.practicum.comment.model.Comment;
import ru.practicum.comment.model.CommentStatus;
import ru.practicum.comment.repository.CommentRepository;
import ru.practicum.utils.error.exception.BadRequestException;
import ru.practicum.utils.error.exception.ConflictException;
import ru.practicum.utils.error.exception.NotFoundException;
import ru.practicum.utils.params.PageParams;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminCommentService {

    private final CommentRepository commentRepository;

    public Page<Comment> getComments(CommentStatus status, Long eventId, PageParams pageParams) {
        Sort sort = Sort.by(Sort.Direction.DESC, "created")
                .and(Sort.by(Sort.Direction.DESC, "id"));
        Pageable pageable = pageParams.toPageable(sort);

        Specification<Comment> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            if (eventId != null) {
                predicates.add(cb.equal(root.get("event").get("id"), eventId));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Comment> page = commentRepository.findAll(spec, pageable);
        log.debug("Найдено {} комментариев (всего подходящих под фильтр: {})",
                page.getNumberOfElements(), page.getTotalElements());
        return page;
    }

    @Transactional
    public Comment updateStatus(Long commentId, CommentStatusUpdateRequest request) {
        if (request.getStatus() != CommentStatus.PUBLISHED && request.getStatus() != CommentStatus.REJECTED) {
            throw new BadRequestException("Target status must be PUBLISHED or REJECTED");
        }

        Comment comment = getCommentById(commentId);

        if (comment.getStatus() != CommentStatus.PENDING) {
            throw new ConflictException("Only comments with status PENDING can be moderated");
        }

        comment.setStatus(request.getStatus());

        if (request.getStatus() == CommentStatus.REJECTED) {
            comment.setRejectionReason(request.getRejectionReason());
        } else {
            comment.setRejectionReason(null);
        }

        log.debug("Админ изменил статус комментария id={} на {}", commentId, request.getStatus());
        return comment;
    }

    @Transactional
    public void delete(Long commentId) {
        Comment comment = getCommentById(commentId);
        commentRepository.delete(comment);
        log.debug("Админ удалил комментарий id={}", commentId);
    }

    private Comment getCommentById(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment with id=" + commentId + " was not found"));
    }
}