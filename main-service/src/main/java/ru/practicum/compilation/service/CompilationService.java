package ru.practicum.compilation.service;


import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.compilation.dto.RequestCompilationDto;
import ru.practicum.compilation.dto.ResponseCompilationDto;
import ru.practicum.compilation.mapper.CompilationMapper;
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
@AllArgsConstructor
public class CompilationService {

    private final CompilationRepository compilationRepository;
    private final CompilationMapper mapper;
    private final EventRepository eventRepository;

    @Transactional
    public ResponseCompilationDto create(RequestCompilationDto dto) {
        Compilation compilation = mapper.toEntity(getEvents(dto.getEvents()), dto);
        Compilation savedCompilation = compilationRepository.save(compilation);
        log.debug("Создана подборка событий = {}", savedCompilation);
        return mapper.toDto(savedCompilation);
    }

    @Transactional
    public void delete(Long compId) {
        Compilation compilation = getCompilationById(compId);
        compilationRepository.delete(compilation);
        log.debug("Удалена подборка : {}", compId);
    }

    private Compilation getCompilationById(Long compId) {
        return compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Compilation with id - " + compId + " was not found."));
    }

    @Transactional
    public ResponseCompilationDto update(Long compId, RequestCompilationDto dto) {
        Compilation compilationEdit = getCompilationById(compId);

        if (dto.getEvents() != null) {
            compilationEdit.setEvents(getEvents(dto.getEvents()));
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
        return mapper.toDto(compilationEdit);
    }

    public List<ResponseCompilationDto> getList(Boolean pinned, PageParams params) {
        log.debug("Пришел запрос с pinned = {} и PageParams = {}", pinned, params);
        Pageable pageable = params.toPageable(Sort.by("id"));
        Page<Compilation> page = (pinned == null)
                ? compilationRepository.findAll(pageable)
                : compilationRepository.findByPinned(pinned, pageable);
        return mapper.toDtoList(page.getContent());
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

    public ResponseCompilationDto getById(Long compId) {
        return compilationRepository.findById(compId)
                .map(mapper::toDto)
                .orElseThrow(() -> new NotFoundException("Not found compilation with id = " + compId));
    }
}