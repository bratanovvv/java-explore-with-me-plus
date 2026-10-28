package ru.practicum.event.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.category.mapper.CategoryMapper;
import ru.practicum.category.model.Category;
import ru.practicum.event.dto.request.EventRequestDto;
import ru.practicum.event.dto.response.EventDto;
import ru.practicum.event.model.Event;
import ru.practicum.user.mapper.UserMapper;

@Component
public class EventMapper {

    private final CategoryMapper categoryMapper;
    private final UserMapper userMapper;
    private final LocationMapper locationMapper;

    public EventMapper(CategoryMapper categoryMapper,
                       UserMapper userMapper,
                       LocationMapper locationMapper) {
        this.categoryMapper = categoryMapper;
        this.userMapper = userMapper;
        this.locationMapper = locationMapper;
    }

    // views и confirmedRequests заполняет EventEnricher, а не маппер.
    public EventDto toDto(Event event) {
        return EventDto.builder()
                .id(event.getId())
                .title(event.getTitle())
                .annotation(event.getAnnotation())
                .category(categoryMapper.toDto(event.getCategory()))
                .paid(event.getPaid())
                .eventDate(event.getEventDate())
                .initiator(userMapper.toDto(event.getInitiator()))
                .description(event.getDescription())
                .participantLimit(event.getParticipantLimit())
                .state(event.getState())
                .createdOn(event.getCreatedOn())
                .publishedOn(event.getPublishedOn())
                .location(locationMapper.toDto(event.getLocation()))
                .requestModeration(event.getRequestModeration())
                .build();
    }

    public Event toEntity(EventRequestDto dto) {
        return Event.builder()
                .annotation(dto.getAnnotation())
                .category(dto.getCategory() == null ? null : toCategoryReference(dto.getCategory()))
                .description(dto.getDescription())
                .eventDate(dto.getEventDate())
                .location(locationMapper.toEntity(dto.getLocation()))
                .paid(dto.getPaid())
                .participantLimit(dto.getParticipantLimit())
                .requestModeration(dto.getRequestModeration())
                .title(dto.getTitle())
                .build();
    }

    private Category toCategoryReference(Long categoryId) {
        return Category.builder().id(categoryId).build();
    }
}
