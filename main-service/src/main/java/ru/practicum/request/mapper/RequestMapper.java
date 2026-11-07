package ru.practicum.request.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.event.model.Event;
import ru.practicum.request.dto.RequestDto;
import ru.practicum.request.model.Request;
import ru.practicum.user.model.User;
import ru.practicum.utils.interfaces.Mapper;

@Component
public class RequestMapper implements Mapper<Request, RequestDto> {
    @Override
    public RequestDto toDto(Request request) {
        return RequestDto.builder()
                .id(request.getId())
                .event(request.getEvent().getId())
                .requester(request.getRequester().getId())
                .created(request.getCreated())
                .status(request.getStatus())
                .build();
    }

    @Override
    public Request toEntity(RequestDto dto) {
        if (dto == null) {
            return null;
        }

        return Request.builder()
                .id(dto.getId())
                .event(toEventReference(dto.getEvent()))
                .requester(toUserReference(dto.getRequester()))
                .created(dto.getCreated())
                .status(dto.getStatus())
                .build();
    }

    private Event toEventReference(Long eventId) {
        if (eventId == null) {
            return null;
        }
        return Event.builder().id(eventId).build();
    }

    private User toUserReference(Long userId) {
        if (userId == null) {
            return null;
        }
        return User.builder().id(userId).build();
    }
}
