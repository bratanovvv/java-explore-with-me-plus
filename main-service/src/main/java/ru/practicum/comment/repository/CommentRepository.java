package ru.practicum.comment.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import ru.practicum.comment.model.Comment;
import ru.practicum.comment.model.CommentStatus;
import ru.practicum.user.model.User;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long>, JpaSpecificationExecutor<Comment> {

    List<Comment> findAllByAuthor(User author, Pageable pageable);

    Page<Comment> findAllByEventIdAndStatus(Long eventId, CommentStatus status, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"author", "event"})
    Page<Comment> findAll(Specification<Comment> spec, Pageable pageable);
}
