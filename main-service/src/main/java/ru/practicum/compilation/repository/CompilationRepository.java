package ru.practicum.compilation.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.compilation.model.Compilation;

import java.util.Optional;

@Repository
public interface CompilationRepository extends JpaRepository<Compilation, Long> {

    @EntityGraph(attributePaths = {"events", "events.category", "events.initiator"})
    Page<Compilation> findByPinned(Boolean pinned, Pageable pageable);

    @EntityGraph(attributePaths = {"events", "events.category", "events.initiator"})
    @Override
    Page<Compilation> findAll(Pageable pageable);

    @Override
    @EntityGraph(
            attributePaths = {"events", "events.category", "events.initiator"},
            type = EntityGraph.EntityGraphType.LOAD
    )
    Optional<Compilation> findById(Long id);
}
