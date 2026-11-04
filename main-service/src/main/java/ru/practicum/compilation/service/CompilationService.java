package ru.practicum.compilation.service;


import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.compilation.repository.CompilationRepository;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.utils.error.exception.BadRequestException;
import ru.practicum.utils.error.exception.NotFoundException;
import ru.practicum.utils.params.PageParams;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@Transactional(readOnly = true)
public class CompilationService {

    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;

    public CompilationService(CompilationRepository compilationRepository, EventRepository eventRepository) {
        this.compilationRepository = compilationRepository;
        this.eventRepository = eventRepository;
    }

    @Transactional
    public Compilation create(Compilation entity, Set<Long> eventsId) {
        entity.setEvents(getEvents(eventsId));
        return compilationRepository.save(entity);
    }

    @Transactional
    public void delete(Long compId) {
        Compilation compilation = getCompilationById(compId);
        compilationRepository.delete(compilation);
        log.debug("Удалена подборка : {}", compId);
    }

    public Compilation getCompilationById(Long compId) {
        return compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Compilation with id - " + compId + " was not found."));
    }

    @Transactional
    public Compilation update(Long compId, CompilationDto dto) {
        Compilation compilationEdit = getCompilationById(compId);

        if (dto.getEventsId() != null) {
            compilationEdit.setEvents(getEvents(dto.getEventsId()));
            log.debug("Изменение Событий на : {}", dto.getEvents());
        }

        if (dto.getPinned() != null) {
            compilationEdit.setPinned(dto.getPinned());
            log.debug("Изменения статуса Зареплено изменено на : {}", dto.getPinned());
        }

        if (dto.getTitle() != null && !dto.getTitle().isBlank()) {
            if (dto.getTitle().length() > 50)
                throw new BadRequestException("Длина title не может быть больше 50 символов");
            compilationEdit.setTitle(dto.getTitle());
            log.debug("Изменение название на : {}", dto.getTitle());
        }
        log.debug("Обновлена подбдорка : {}", compId);
        return compilationEdit;
    }

    public List<Compilation> getList(Boolean pinned, PageParams params) {
        log.debug("Пришел запрос с pinned = {} и PageParams = {}", pinned, params);
        Pageable pageable = params.toPageable(Sort.by("id"));
        Page<Compilation> page = (pinned == null)
                ? compilationRepository.findAll(pageable)
                : compilationRepository.findByPinned(pinned, pageable);
        return page.getContent();
    }


    private Set<Event> getEvents(Set<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) return new HashSet<>();
        Set<Event> eventSet = new HashSet<>(eventRepository.findAllById(eventIds));
        if (eventSet.size() != eventIds.size()) {
            Set<Long> missingEvents = new HashSet<>(eventIds);
            eventSet.forEach(e -> missingEvents.remove(e.getId()));
            throw new NotFoundException("Events not found: " + missingEvents);
        }
        return eventSet;
    }
}